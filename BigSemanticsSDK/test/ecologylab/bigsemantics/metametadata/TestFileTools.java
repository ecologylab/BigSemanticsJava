package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * @author quyin
 * 
 */
public class TestFileTools
{

  @Test
  public void testGetRelativePath()
  {
    validateRelativePath("/usr/bin/", "/usr/bin/java", '/', "java");
    validateRelativePath("/usr/bin", "/usr/bin/java", '/', "java");
    validateRelativePath("/usr/bin", "/usr/bin//java", '/', "java");
    validateRelativePath("usr/bin", "usr/bin/jdk/java", '/', "jdk/java");
    validateRelativePath("/usr/bin", "/tmp/java", '/', "../../tmp/java");
    validateRelativePath("/usr/bin", "/usr/local/bin/java", '/', "../local/bin/java");
    validateRelativePath("/", "/tmp/java", '/', "tmp/java");

    validateRelativePath("C:\\", "C:\\programs\\jdk\\java.exe", '\\', "programs\\jdk\\java.exe");
    validateRelativePath("C:\\users", "C:\\programs\\\\java.exe", '\\', "..\\programs\\java.exe");
    validateRelativePath("C:\\", "D:\\programs\\jdk\\java.exe", '\\', "D:\\programs\\jdk\\java.exe");
  }

  private void validateRelativePath(String ancestorPath,
                                    String filePath,
                                    char separator,
                                    String expectedRelativePath)
  {
    String result = FileTools.getRelativePath(ancestorPath, filePath, separator);
    System.out.format("ancestor: %s, file: %s, expected: %s, actual: %s\n",
                      ancestorPath,
                      filePath,
                      expectedRelativePath,
                      result);
    assertEquals(expectedRelativePath, result);
  }

}
