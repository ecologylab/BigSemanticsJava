package ecologylab.bigsemantics.metametadata;

import java.util.Map;

import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.collections.MultiAncestorScope;

/**
 * a scope of generic type vars. also handles type checking, generic name resolving, etc.
 * 
 * @author quyin
 * 
 */
public class MmdGenericTypeVarScope extends MultiAncestorScope<MmdGenericTypeVar>
{

	public void inheritFrom(MmdGenericTypeVarScope superScope, InheritanceHandler inheritanceHandler)
	{
		if (superScope == null)
			return;

		for (MmdGenericTypeVar superGenericTypeVar : superScope.values())
		{
			String name = superGenericTypeVar.getName();
			MmdGenericTypeVar localGenericTypeVar = this.get(name);

			if (localGenericTypeVar == null)
			{
				this.put(name, superGenericTypeVar);
			}
			else
			{
				localGenericTypeVar.resolveArgAndBounds((Map) this);

				if (superGenericTypeVar.isAssignment() && localGenericTypeVar.isAssignment()
						&& !superGenericTypeVar.getArg().equals(localGenericTypeVar.getArg()))
				{
					throw new MetaMetadataException("incompatiable assignments to a generic type var: "
							+ name);
				}
				else if (superGenericTypeVar.isAssignment() && localGenericTypeVar.isBound())
				{
					throw new MetaMetadataException("generic type already assigned: " + name);
				}
				else if (superGenericTypeVar.isBound() && localGenericTypeVar.isAssignment())
				{
					checkAssignmentWithBounds(name, localGenericTypeVar, superGenericTypeVar,
							inheritanceHandler);
				}
				else
				{
					checkBoundsWithBounds(name, localGenericTypeVar, superGenericTypeVar, inheritanceHandler);
				}
			}
		}
	}

	private void checkAssignmentWithBounds(String name, MmdGenericTypeVar argGtv,
			MmdGenericTypeVar boundGtv, InheritanceHandler inheritanceHandler)
	{
		MetaMetadata argMmd = inheritanceHandler.resolveMmdName(argGtv.getArg());
		argMmd.inheritMetaMetadata();

		MetaMetadata lowerBoundMmd = inheritanceHandler.resolveMmdName(boundGtv.getExtendsAttribute());
		lowerBoundMmd.inheritMetaMetadata();
		boolean satisfyLowerBound = lowerBoundMmd == null || argMmd.isDerivedFrom(lowerBoundMmd);

		// MetaMetadata upperBoundMmd = inheritanceHandler.resolveMmdName(localGtv.getSuperAttribute());
		// boolean satisfyUpperBound = upperBoundMmd == null || upperBoundMmd.isDerivedFrom(argMmd);

		if (!satisfyLowerBound /* || !satisfyUpperBound */)
			throw new MetaMetadataException("generic type bound(s) not satisfied: " + name
					                            + ": arg type=" + argMmd + ", lowerBound=" + lowerBoundMmd);
	}

	private void checkBoundsWithBounds(String name, MmdGenericTypeVar local, MmdGenericTypeVar other,
			InheritanceHandler inheritanceHandler)
	{
		MetaMetadata lowerBoundMmdLocal = inheritanceHandler.resolveMmdName(local.getExtendsAttribute());
		lowerBoundMmdLocal.inheritMetaMetadata();
		
		MetaMetadata lowerBoundMmdOther = inheritanceHandler.resolveMmdName(other.getExtendsAttribute());
		lowerBoundMmdOther.inheritMetaMetadata();
		
		boolean lowerBoundsCompatible = lowerBoundMmdOther == null
				|| lowerBoundMmdLocal.isDerivedFrom(lowerBoundMmdOther);

		// TODO upperBoundsCompatible

		if (!lowerBoundsCompatible /* || !upperBoundsCompatible */)
			throw new MetaMetadataException("generic type bound(s) not compatible: " + name);
	}

}
