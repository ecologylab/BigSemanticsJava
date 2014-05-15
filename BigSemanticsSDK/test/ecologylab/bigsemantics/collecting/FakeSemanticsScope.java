package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.downloadcontrollers.DownloadController;
import ecologylab.bigsemantics.downloadcontrollers.FakeDownloadControllerFactory;
import ecologylab.bigsemantics.html.dom.IDOMProvider;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.SimplTypesScope;

/**
 * A fake semantics scope that allows us to change its internals for testing.
 * 
 * @author quyin
 */
@SuppressWarnings("serial")
public class FakeSemanticsScope extends SemanticsSessionScope
{

  private FakeDownloadControllerFactory factory;

  public FakeSemanticsScope(SimplTypesScope metadataTypesScope,
                            Class<? extends IDOMProvider> domProviderClass)
  {
    super(metadataTypesScope, domProviderClass);
  }

  /**
   * Make this scope to use the specified download controller factory which allows us to use fake
   * download controllers for testing.
   * 
   * @param factory
   */
  public void setFakeDownloadControllerFactory(FakeDownloadControllerFactory factory)
  {
    this.factory = factory;
  }

  @Override
  public DownloadController createDownloadController(DocumentClosure closure)
  {
    return factory.createDownloadController();
  }

}
