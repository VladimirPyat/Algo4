import java.util.Arrays;
import java.util.Iterator;

public class HashMap<K, V> implements Iterable<HashMap.Entity> {


    private static final int INIT_BUCKET_COUNT = 16;
    private static final double LOAD_FACTOR = 0.5;

    private Bucket[] buckets;
    private int size;


    @Override
    public Iterator<HashMap.Entity> iterator() {
        return new HashMapIterator(buckets);
    }

    class HashMapIterator implements Iterator<HashMap.Entity> {
        private Bucket[] buckets;
        private int currentIndex;
        private Bucket.BucketIterator currentBucketIterator;

        public HashMapIterator(Bucket[] buckets) {
            this.buckets = new HashMap.Bucket[buckets.length];
            System.arraycopy(buckets, 0, this.buckets, 0, buckets.length);
            this.currentIndex = 0;
            this.currentBucketIterator = null;
        }

        @Override
        public boolean hasNext() {

            if (currentBucketIterator != null && currentBucketIterator.hasNext()) {
                return true;
            }

            while (currentIndex < buckets.length-1) {
                currentIndex++;
                if (buckets[currentIndex] != null) {
                    currentBucketIterator = (Bucket.BucketIterator) buckets[currentIndex].iterator();
                    if (currentBucketIterator.hasNext()) {
                        return true;
                    }
                }
//                currentIndex++;
            }

            return false;
        }

        @Override
        public Entity next() {
            if (currentBucketIterator.hasNext()) {
                return currentBucketIterator.next();
            }

            if (currentIndex < buckets.length) {
                currentBucketIterator = (Bucket.BucketIterator) buckets[currentIndex].iterator();
                if (currentBucketIterator.hasNext()) {
                    return currentBucketIterator.next();
                }
            }
            currentIndex++;
            return null;
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (HashMap<String, String>.Entity element : this) {
            sb.append(element.value);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Элемент хеш-таблицы
     */
    class Entity{

        /**
         * Ключ
         */
        K key;

        /**
         * Значение элемента
         */
        V value;

    }

    /**
     * Бакет, связный список
     */
    class Bucket implements Iterable <Entity>{

        @Override
        public Iterator<Entity> iterator() {
            return new BucketIterator();
        }

        public class BucketIterator implements Iterator <Entity> {
            private Node currentNode;


            public BucketIterator() {
                this.currentNode = head;

            }

            @Override
            public boolean hasNext() {
                boolean result = currentNode != null;
                return result;
            }

            @Override
            public Entity next() {
                if (hasNext()) {
                    Entity result = this.currentNode.value;
                    this.currentNode = this.currentNode.next;
                    return result;
                }
                return null;

            }
        }

        /**
         * Указатель на первый элемент связного списка
         */
        Node head;

        /**
         * Узел бакета (связного списка)
         */
        class Node{

            /**
             * Указатель на следующий элемент связного списка
             */
            Node next;

            /**
             * Значение узла, указывающее на элемент хеш-таблицы
             */
            Entity value;

        }

        public V add(Entity entity){
            Node node = new Node();
            node.value = entity;

            if (head == null){
                head = node;
                return null;
            }

            Node currentNode = head;
            while (true){
                if (currentNode.value.key.equals(entity.key)){
                    V buf = currentNode.value.value;
                    currentNode.value.value = entity.value;
                    return buf;
                }
                if (currentNode.next != null){
                    currentNode = currentNode.next;
                }
                else {
                    currentNode.next = node;
                    return null;
                }
            }
        }

        public V remove(K key){
            if (head == null)
                return null;
            if (head.value.key.equals(key)){
                V buf = head.value.value;
                head = head.next;
                return buf;
            }
            else {
                Node node = head;
                while (node.next != null){
                    if (node.next.value.key.equals(key)){
                        V buf = node.next.value.value;
                        node.next = node.next.next;
                        return buf;
                    }
                    node = node.next;
                }
                return null;
            }
        }

        public V get(K key){
            Node node = head;
            while (node != null){
                if (node.value.key.equals(key))
                    return node.value.value;
                node = node.next;
            }
            return null;
        }

    }

    private int calculateBucketIndex(K key){
        return Math.abs(key.hashCode()) % buckets.length;
    }

    private void recalculate(){
        size = 0;
        Bucket[] old = buckets;
        buckets = new HashMap.Bucket[old.length * 2];
        for (int i = 0; i < old.length; i++){
            Bucket bucket = old[i];
            if (bucket != null){
                Bucket.Node node = bucket.head;
                while (node != null){
                    put(node.value.key, node.value.value);
                    node = node.next;
                }
            }
        }
    }

    public V put(K key, V value){
        if (size >= buckets.length * LOAD_FACTOR ){
            recalculate();
        }
        int index = calculateBucketIndex(key);
        Bucket bucket = buckets[index];
        if (bucket == null){
            bucket = new Bucket();
            buckets[index] = bucket;
        }

        Entity entity = new Entity();
        entity.key = key;
        entity.value = value;

        V buf = bucket.add(entity);
        if (buf == null){
            size++;
        }
        return buf;
    }

    public V get(K key){
        int index = calculateBucketIndex(key);
        Bucket bucket = buckets[index];
        if (bucket == null)
            return null;
        return bucket.get(key);
    }

    public V remove(K key){
        int index = calculateBucketIndex(key);
        Bucket bucket = buckets[index];
        if (bucket == null)
            return null;
        V buf = bucket.remove(key);
        if (buf != null){
            size--;
        }
        return buf;
    }

    public HashMap(){
        buckets = new HashMap.Bucket[INIT_BUCKET_COUNT];
    }

    public HashMap(int initCount){
        buckets = new HashMap.Bucket[initCount];
    }


}
