package scout.map

import scout.definition.ObjectKey
import scout.factory.InstanceFactory

internal typealias ObjectFactoryMap = IntMap<InstanceFactory<*>>

internal fun createObjectFactoryMap(map: Map<ObjectKey, InstanceFactory<*>>): ObjectFactoryMap {
    return ObjectFactoryMap(map)
}