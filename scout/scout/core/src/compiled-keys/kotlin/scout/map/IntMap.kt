package scout.map

class IntMap<out V>(initMap: Map<Int, V>) {
    // placeholder
    private val hashmap: HashMap<Int, V>
    init {
        // TODO init
        hashmap = HashMap(initMap)
    }

    operator fun get(key: Int): V? {
        // TODO get
        return hashmap[key]
    }

    fun toMap(): Map<Int, V> {
        // TODO convert this map into regular Map<Int, V>
        return hashmap
    }
}