package ecologylab.semantics.metametadata;

import java.util.List;
import java.util.Stack;

import ecologylab.collections.MultiAncestorScope;
import ecologylab.generic.Debug;
import ecologylab.generic.StringTools;

/**
 * <b>Note:</b>
 * 
 * this class is added to separated inheritance logics from MetaMetadata*Field classes, or at least
 * part of them. implementor of this class should carefully design the separation of different
 * concerns during the inheritance process, including:
 * 
 * <ul>
 * <li>map from name to meta-metadata objects</li>
 * <li>inheriting attributes and elements</li>
 * <li>generics</li>
 * <li>ad hoc meta-metadata types</li>
 * <li>local SimplTypesScopes to handle conflicting meta-metadata tag names for direct binding</li>
 * <li>extraction of individual elements in a collection</li>
 * </ul>
 * 
 * this class is now incomplete.
 * 
 * @author quyin
 * 
 */
public class InheritanceHandler implements InheritanceComponentNames, Cloneable
{

	MetaMetadataRepository						repository;
	
	MetaMetadata											rootMmd;

	/**
	 * this keeps track of meta-metadata field objects.
	 */
	private Stack<MetaMetadataField>	mmStack			= new Stack<MetaMetadataField>();

	/**
	 * this maintains a stack of scopes containing things that pass from upper level structures to
	 * lower level structures, e.g. meta-metadata types, generic type vars, etc. somewhat similar
	 * concept to a lexical scope.
	 */
	private Stack<MultiAncestorScope>	scopeStack	= new Stack<MultiAncestorScope>();
	
	public InheritanceHandler(MetaMetadata rootMmd)
	{
		this.rootMmd = rootMmd;
		this.repository = rootMmd.getRepository();
	}

	void push(MetaMetadataField mmField)
	{
		Debug.println(this, "pushing " + mmField);
		mmStack.push(mmField);

		// put mmd scope
		MultiAncestorScope scope = new MultiAncestorScope();
		if (scopeStack.size() > 0)
			scope.addAncestor(scopeStack.peek());
		scopeStack.push(scope);

		// put generic type var scope
		MmdGenericTypeVarScope existingMmdGenericTypeVarScope = (MmdGenericTypeVarScope) scope.get(GENERIC_TYPE_VAR_SCOPE);
		MmdGenericTypeVarScope currentMmdGenericTypeVarScope = mmField.genericTypeVars;
		if (currentMmdGenericTypeVarScope != null && existingMmdGenericTypeVarScope != null)
		{
			currentMmdGenericTypeVarScope.inheritFrom(existingMmdGenericTypeVarScope, this);
		}
		scope.putIfValueNotNull(GENERIC_TYPE_VAR_SCOPE, mmField.genericTypeVars);
	}

	void pop(MetaMetadataField mmField)
	{
		MetaMetadataField field = mmStack.pop();
		Debug.println(this, "popping " + field);
		scopeStack.pop();
		assert (mmField == field);
	}

	public boolean canReset()
	{
		// TODO sometimes the object may not be reset.
		return true;
	}

	/**
	 * reset the state of the inheritance handler, as if it is newly created. this allows for pooling
	 * and reusing of the object.
	 */
	public void reset()
	{
		// TODO
	}

	MetaMetadata resolveMmdName(String mmdName)
	{
		return resolveMmdName(mmdName, null);
	}
	
	public static enum NameType { NONE, MMD, GENERIC };
	
	MetaMetadata resolveMmdName(String mmdName, NameType[] nameType)
	{
		if (mmdName == null)
			return null;
		MetaMetadata result = null;
		MetaMetadataField field = mmStack.peek();
		if (nameType != null && nameType.length > 0)
			nameType[0] = NameType.NONE;
		
		// step 1: try to resolve the name as a concrete meta-metadata name, using the mmdScope.
		if (field instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) field;
			result = (MetaMetadata) nested.getMmdScope().get(mmdName);
			if (result != null)
				if (nameType != null && nameType.length > 0)
					nameType[0] = NameType.MMD;
		}

		// step 2: if step 1 failed, try to use it as a generic type var name
		if (result == null && StringTools.isUpperCase(mmdName))
		{
			List<MmdGenericTypeVarScope> gtvScopes = scopeStack.peek().getAll(GENERIC_TYPE_VAR_SCOPE);
			for (MmdGenericTypeVarScope gtvScope : gtvScopes)
			{
				MmdGenericTypeVar gtv = gtvScope.get(mmdName);
				if (gtv != null)
				{
					if (gtv.getArg() != null)
						result = resolveMmdName(gtv.getArg());
					else if (gtv.getExtendsAttribute() != null)
						result = resolveMmdName(gtv.getExtendsAttribute());
					// TODO superAttribute?
				}
			}
			if (result != null)
				if (nameType != null && nameType.length > 0)
					nameType[0] = NameType.GENERIC;
		}

		return result;
	}
	
	public boolean isUsingGenerics(MetaMetadataField field)
	{
		if (field.genericTypeVars != null && field.genericTypeVars.size() > 0)
			return true;
		MmdGenericTypeVarScope gtvScope = (MmdGenericTypeVarScope) scopeStack.peek().get(
				GENERIC_TYPE_VAR_SCOPE);
		if (gtvScope == null)
			return false;
		if (gtvScope.containsKey(field.getType()) || field.getType() == null
				&& gtvScope.containsKey(field.getName())
				|| gtvScope.containsKey(field.getExtendsAttribute()))
			return true;
		return false;
	}

	public String toString()
	{
		return this.getClass().getSimpleName() + "[" + rootMmd.getName() + "]";
	}
	
	public InheritanceHandler clone()
	{
		InheritanceHandler cloned = new InheritanceHandler(rootMmd);
		cloned.repository = this.repository;
		cloned.mmStack = new Stack<MetaMetadataField>();
		cloned.mmStack.addAll(this.mmStack);
		cloned.scopeStack = new Stack<MultiAncestorScope>();
		cloned.scopeStack.addAll(this.scopeStack);
		return cloned;
	}

//	public void addGenericTypeVarScope(MmdGenericTypeVarScope genericTypeVarScope)
//	{
//		if (scopeStack.size() > 0)
//		{
//			MultiAncestorScope scope = scopeStack.peek();
//			scope.put(GENERIC_TYPE_VAR_SCOPE, genericTypeVarScope);
//		}
//	}

}
