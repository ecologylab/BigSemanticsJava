package ecologylab.semantics.collecting;

/**
 * Create Value objects for hashed data structure with synchronized writes and unsynchronized reads.
 * @author andruid
 *
 * @param <K>
 * @param <V>
 */
public interface ConditionalValueFactory<K, V>
{
	/**
	 * Construct a value; the procedure is conditionally dependent on the 2nd parameter.
	 * 
	 * @param key
	 * @param isImage
	 * @return
	 */
	public V constructValue(K key, boolean isImage);
}
