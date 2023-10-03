/*
 * Copyright 2023 Yandex LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scout

import org.apache.bcel.classfile.ClassParser
import org.apache.bcel.generic.ClassGen
import org.apache.bcel.generic.ConstantPoolGen
import org.apache.bcel.generic.INVOKEINTERFACE
import org.apache.bcel.generic.INVOKESTATIC
import org.apache.bcel.generic.Instruction
import org.apache.bcel.generic.InstructionConst
import org.apache.bcel.generic.InstructionHandle
import org.apache.bcel.generic.InstructionList
import org.apache.bcel.generic.LDC
import org.apache.bcel.generic.MethodGen
import org.apache.bcel.generic.ObjectType
import org.apache.bcel.generic.SIPUSH
import org.apache.bcel.generic.TargetLostException
import org.apache.bcel.generic.Visitor
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

class ScoutCompileKeysFeature(private val project: Project) {
    private val scoutMapsFile = File(project.buildDir, MAPS_FILE)
    private val objectKeysMap = HashMap<String, Int>()
    private val collectionKeysMap = HashMap<String, Int>()
    private val associationKeysMap = HashMap<String, Int>()

    fun apply(generateCompiledKeyMapper: Boolean) {
        inflateProjectBuildDir()
        loadMapFile()
        project.allprojects { project ->
            project.tasks.configureEach { task ->
                if (task.name.startsWith("compile") && task.name.endsWith("Kotlin")) {
                    task.doLast(ModifyBytecodeAction(generateCompiledKeyMapper))
                }
            }
        }
    }

    /**
     * Inflates build directory to provide correct `appendText` behaviour.
     */
    private fun inflateProjectBuildDir() {
        scoutMapsFile.parentFile.mkdirs()
    }

    /**
     * Loads the mapping file containing information about object keys, collection keys,
     * and association keys. This file is used to map these keys to corresponding integer constants.
     *
     * The method reads from a file named 'scoutMapsFile' and populates three maps:
     * - objectKeysMap: contains mappings for objects identified by [OBJECT_TYPE]
     * - collectionKeysMap: contains mappings for collections identified by [COLLECTION_TYPE]
     * - associationKeysMap: contains mappings for associations identified by [ASSOCIATION_TYPE]
     *
     * The mapping file is expected to have a specific format where each line consists of three parts:
     * - Type: an integer representing the type of the mapping (object, collection, or association).
     * - Name: a string representing the name or identification of the item.
     * - Key: an integer representing the corresponding key.
     *
     * The parts are separated by semicolons (';'). For example, a line might look like this:
     *
     * "1;MyClass;42"
     *
     * If the mapping file does not exist, the method returns without making any changes.
     */
    private fun loadMapFile() {
        if (!scoutMapsFile.exists()) {
            return
        }
        scoutMapsFile.useLines { lines ->
            lines.forEach { line ->
                val (type, name,_, key) = line.split(' ')
                val map = when (type) {
                    Type.Object.name -> objectKeysMap
                    Type.Collection.name -> collectionKeysMap
                    Type.Association.name -> associationKeysMap
                    else -> error("Unknown map type $type")
                }
                map[name] = key.toInt()
            }
        }
    }

    private inner class ModifyBytecodeAction(private val generateCompiledKeyMapper: Boolean) : Action<Task> {

        /**
         * Executes the specified action, scanning task's output files for class files and applying specific
         * modifications related to object, collection, and association key creation. The method also identifies
         * the CompileKeyMapper class, if present, and performs additional modifications if required.
         *
         * @param task The task to execute.
         * @throws IllegalStateException If more than one CompileKeyMapper is found.
         * @see modifyClass
         * @see modifyCompileKeyMapper
         */
        override fun execute(task: Task) {
            val mapperFiles = mutableListOf<File>()
            task.outputs.files.forEach {
                it.walk()
                    .filter { file -> file.isFile && file.extension == "class" }
                    .forEach { file ->
                        if (modifyClass(file)) {
                            mapperFiles += file
                        }
                    }
            }

            if (generateCompiledKeyMapper) {
                mapperFiles.forEach(::modifyCompileKeyMapper)
            }
        }

        /**
         * Modifies the specified class file by transforming specific patterns related to
         * object, collection, and association key creation. The method scans through the methods
         * of the class and applies the transformation defined in the [modifyMethod] function.
         *
         * This method also checks whether the class is a mapper class
         *
         * If any modifications were made to the class, the class is re-dumped to the file system.
         *
         * @param file The class file to modify.
         * @return A boolean indicating whether the class is a mapper class. The return value does
         *         not reflect whether any modifications were made to the class itself.
         *
         * @see modifyMethod
         */
        private fun modifyClass(file: File): Boolean {
            val parser = ClassParser(file.path)
            val javaClass = parser.parse()

            val classGen = ClassGen(javaClass)
            val constantPoolGen = classGen.constantPool

            var changed = false

            for (method in classGen.methods) {
                val methodGen = MethodGen(method, classGen.className, constantPoolGen)

                if (modifyMethod(methodGen.instructionList ?: continue, constantPoolGen)) {
                    classGen.replaceMethod(method, methodGen.method)
                    changed = true
                }
            }

            if (changed) {

                classGen.javaClass.dump(FileOutputStream(file))
            }

            return "inlined\$$SCOUT_INIT_COMPILE_KEY_MAPPER_METHOD_NAME" in javaClass.className &&
                    javaClass.methods.any { it.name == COMPILE_KEY_MAPPER_METHOD_NAME && !it.isAbstract }
        }

        /**
         * Modifies the given instruction list by identifying and transforming specific patterns related to
         * object, collection, and association key creation. Specifically, the method replaces occurrences
         * of [ObjectKeys.create], [CollectionKeys.create], or [AssociationKeys.create] with corresponding
         * integer constants based on the class name.
         *
         * The transformation targets the following patterns:
         * 1. LDC instruction followed by INVOKESTATIC instruction, which corresponds to the invocation of the `create` method.
         * (in case of [AssociationKeys.create] there are 2 LDC instrutions followed by INVOKESTATIC)
         * 2. Replaces the matched pattern with a SIPUSH instruction, pushing an integer constant onto the stack.
         *
         * @param list The instruction list to modify.
         * @param constantPoolGen The constant pool generator, used to access constant pool information.
         * @return A boolean indicating whether any modifications were made to the instruction list.
         *
         * @throws IllegalStateException if the project has more than 32,768 dependencies (limit for the SIPUSH instruction).
         */
        private fun modifyMethod(
            list: InstructionList,
            constantPoolGen: ConstantPoolGen
        ): Boolean {
            var changed = false
            var instruction: InstructionHandle? = null

            while (true) {
                instruction = (if (instruction == null) list.start else instruction.next) ?: break

                val ldc2 = instruction.instruction as? LDC ?: continue
                val className2 = (ldc2.getValue(constantPoolGen) as? ObjectType)?.className ?: continue

                val invokestatic = instruction.next?.instruction as? INVOKESTATIC ?: continue
                val invokeStaticClassName = invokestatic.getLoadClassType(constantPoolGen).className

                if (invokestatic.getName(constantPoolGen) != CREATE_METHOD_NAME) {
                    continue
                }

                val ldc1 = instruction.prev?.instruction as? LDC
                val className1 = (ldc1?.getValue(constantPoolGen) as? ObjectType)?.className

                val indexMap: Map<String, Int>
                val className: String
                val type: Type
                val signature: String

                when (invokeStaticClassName) {
                    OBJECT_KEYS_CLASS_NAME -> {
                        signature = OBJECT_KEYS_CREATE_METHOD_SIGNATURE
                        indexMap = objectKeysMap
                        type = Type.Object
                        className = className2
                    }

                    COLLECTION_KEYS_CLASS_NAME -> {
                        signature = COLLECTION_KEYS_CREATE_METHOD_SIGNATURE
                        indexMap = collectionKeysMap
                        type = Type.Collection
                        className = className2
                    }

                    ASSOCIATION_KEYS_CLASS_NAME -> {
                        signature = ASSOCIATION_KEYS_CREATE_METHOD_SIGNATURE
                        indexMap = associationKeysMap
                        type = Type.Association
                        className = className1.toString() + ASSOCIATION_NAME_KEY_DIVIDER + className2
                    }

                    else -> continue
                }

                if (invokestatic.getSignature(constantPoolGen) != signature) {
                    continue
                }

                val index = getIndex(indexMap, className, type)

                check(index < 32_768) {
                    "Project must have less then 32k factories"
                }

                if (type == Type.Association) {
                    instruction.prev.instruction = NoOperation(instruction.prev.instruction.length)
                }

                instruction.instruction = NoOperation(instruction.instruction.length)

                val sipush = SIPUSH(index.toShort())
                require(instruction.next.instruction.length == sipush.length)
                instruction.next.instruction = sipush
                changed = true
            }

            return changed
        }

        /**
         * Retrieves or creates an index value for a given class name and type. The index value is a unique integer
         * identifier associated with a specific class name within a particular type category (Object, Collection,
         * or Association).
         *
         * If the class name is already present in the provided map, the existing index value is returned.
         * If the class name is not found in the map, a new index is generated, added to the map, and
         * appended to the scoutMapsFile.
         *
         * This method is synchronized on the index map to ensure thread-safety during index retrieval or creation.
         *
         * @param indexMap A mutable map containing existing index values, keyed by class name.
         * @param className The fully-qualified class name for which to retrieve or create an index.
         * @param type Enum representing the type category (e.g., Object, Collection, or Association).
         * @return The retrieved or newly created index value.
         */
        private fun getIndex(indexMap: MutableMap<String, Int>, className: String, type: Type): Int {
            // Reserve builtin type keys
            when (className) {
                Lazy::class.qualifiedName -> return -1
                "scout.Provider" -> return -2
                Unit::class.qualifiedName -> return -3
            }
            val typeName = type.name
            synchronized(indexMap) {
                val stored = indexMap[className]

                if (stored == null) {
                    val newIndex = indexMap.size
                    indexMap[className] = newIndex
                    scoutMapsFile.appendText("$typeName $className -> $newIndex\n")

                    return newIndex
                }

                return stored
            }
        }
    }

    /**
     * Modifies the CompileKeyMapper class, generating specific bytecode instructions to populate the keys map.
     * This method builds the instructions for storing object, collection, and association keys in the corresponding
     * maps, utilizing the SIPUSH, LDC, and INVOKEINTERFACE instructions.
     *
     * The method constructs the following patterns for each key type:
     * 1. For Object Keys:
     *    - ALOAD_1: Load the first argument (the keys map) onto the stack.
     *    - SIPUSH: Push the short integer value of the key.
     *    - LDC: Load the class name as a constant.
     *    - INVOKEINTERFACE: Call the `putObjectKey` method on the keys map interface.
     *
     *    Those instruction represent this Java code:
     *
     *         keysMap.putObjectKey(42, "com/example/Foo");
     *
     * 2. For Collection Keys:
     *    - Similar to object keys, but calls the `putCollectionKey` method.
     * 3. For Association Keys:
     *    - Similar to object keys, but loads two class names and calls the `putAssociationKey` method.
     *
     * @param file The file representing the CompileKeyMapper class to modify.
     */
    private fun modifyCompileKeyMapper(file: File) {
        val parser = ClassParser(file.path)
        val javaClass = parser.parse()

        val cg = ClassGen(javaClass)
        val cp = cg.constantPool
        val m = cg.methods.first { it.name == COMPILE_KEY_MAPPER_METHOD_NAME }
        val method = MethodGen(
            m,
            cg.className,
            cp
        )

        method.maxStack = 4
        val il = method.instructionList

        try {
            il.delete(il.start, il.end)
        } catch (e: TargetLostException) {
            // Ignore exceptions
        }

        val putObjectMethodIndex = cp.addInterfaceMethodref(
            KEYS_MAP_CLASS_NAME,
            PUT_OBJECT_KEY_METHOD_NAME,
            PUT_OBJECT_KEY_METHOD_SIGNATURE
        )
        for ((name, key) in objectKeysMap) {
            il.append(InstructionConst.ALOAD_1)
            il.append(SIPUSH(key.toShort()))
            il.append(LDC(cp.addString(name)))
            il.append(INVOKEINTERFACE(putObjectMethodIndex, 3))
        }

        val putCollectionMethodIndex = cp.addInterfaceMethodref(
            KEYS_MAP_CLASS_NAME,
            PUT_COLLECTION_KEY_METHOD_NAME,
            PUT_COLLECTION_KEY_METHOD_SIGNATURE
        )
        for ((name, key) in collectionKeysMap) {
            il.append(InstructionConst.ALOAD_1)
            il.append(SIPUSH(key.toShort()))
            il.append(LDC(cp.addString(name)))
            il.append(INVOKEINTERFACE(putCollectionMethodIndex, 3))
        }

        val putAssociationMethodIndex = cp.addInterfaceMethodref(
            KEYS_MAP_CLASS_NAME,
            PUT_ASSOCIATION_KEY_METHOD_NAME,
            PUT_ASSOCIATION_KEY_METHOD_SIGNATURE
        )
        for ((name, key) in associationKeysMap) {
            val (first, second) = name.split(ASSOCIATION_NAME_KEY_DIVIDER)
            il.append(InstructionConst.ALOAD_1)
            il.append(SIPUSH(key.toShort()))
            il.append(LDC(cp.addString(first)))
            il.append(LDC(cp.addString(second)))
            il.append(INVOKEINTERFACE(putAssociationMethodIndex, 4))
        }

        il.append(InstructionConst.RETURN)

        cg.replaceMethod(m, method.method)
        cg.javaClass.dump(file)
    }



    /**
     * Represents a No-Operation (NOP) instruction within the bytecode.
     * This class extends the `Instruction` class, allowing it to be used as a placeholder
     * where a specific operation is not required.
     *
     * The `NoOperation` class is used to replace certain instructions with a NOP
     * instruction, effectively neutralizing the original instruction while maintaining
     * the bytecode size.
     *
     * The dumped bytecode for this instruction consists of zero-value bytes with a length
     * equal to the provided `length` parameter.
     *
     * @param length The length of the NOP instruction in bytes. This defines the number of zero-value bytes that will be written.
     */
    private class NoOperation(length: Int) : Instruction(0, length.toShort()) {

        /**
         * Writes the NOP instruction to the provided DataOutputStream.
         * The output consists of zero-value bytes repeated for the length of the instruction.
         */
        override fun dump(out: DataOutputStream) {
            repeat(getLength()) {
                out.writeByte(0)
            }
        }

        override fun accept(v: Visitor?) {
        }
    }

    private enum class Type {
        Object,
        Collection,
        Association
    }

    companion object {
        private const val MAPS_FILE = "scout-maps-file.txt"

        private const val SCOUT_INIT_COMPILE_KEY_MAPPER_METHOD_NAME = "initCompileKeyMapper"

        private const val CREATE_METHOD_NAME = "create"

        private const val OBJECT_KEYS_CLASS_NAME = "scout.definition.ObjectKeys"
        private const val OBJECT_KEYS_CLASS_NAME_WITH_SLASHES = "scout/definition/ObjectKeys"
        private const val OBJECT_KEYS_CREATE_METHOD_SIGNATURE = "(Ljava/lang/Class;)I"

        private const val COLLECTION_KEYS_CREATE_METHOD_SIGNATURE = "(Ljava/lang/Class;)I"
        private const val COLLECTION_KEYS_CLASS_NAME = "scout.definition.CollectionKeys"
        private const val COLLECTION_KEYS_CLASS_NAME_WITH_SLASHES = "scout/definition/CollectionKeys"

        private const val ASSOCIATION_KEYS_CLASS_NAME = "scout.definition.AssociationKeys"
        private const val ASSOCIATION_KEYS_CLASS_NAME_WITH_SLASHES = "scout/definition/AssociationKeys"
        private const val ASSOCIATION_KEYS_CREATE_METHOD_SIGNATURE = "(Ljava/lang/Class;Ljava/lang/Class;)I"

        private const val COMPILE_KEY_MAPPER_METHOD_NAME = "__init_mapping__"

        private const val KEYS_MAP_CLASS_NAME = "scout.mapper.KeysMap"

        private const val PUT_OBJECT_KEY_METHOD_NAME = "putObjectKey"
        private const val PUT_OBJECT_KEY_METHOD_SIGNATURE = "(ILjava/lang/String;)V"

        private const val PUT_COLLECTION_KEY_METHOD_NAME = "putCollectionKey"
        private const val PUT_COLLECTION_KEY_METHOD_SIGNATURE = "(ILjava/lang/String;)V"

        private const val PUT_ASSOCIATION_KEY_METHOD_NAME = "putAssociationKey"
        private const val PUT_ASSOCIATION_KEY_METHOD_SIGNATURE = "(ILjava/lang/String;Ljava/lang/String;)V"

        private const val ASSOCIATION_NAME_KEY_DIVIDER = "<->"
    }
}
