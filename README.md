[![Build Status](https://travis-ci.org/STAMP-project/AssertFixer.svg?branch=master)](https://travis-ci.org/STAMP-project/AssertFixer)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/AssertFixer/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/AssertFixer?branch=master)
AssertFixer
=====================================================================================================================

This project aims at fixing assertion. The program under test is used as specification to fix the test suite.

See: [Brett Daniel et al.](https://scholar.google.com/citations?view_op=view_citation&hl=fr&user=x6OIBq4AAAAJ&citation_for_view=x6OIBq4AAAAJ:roLk4NBRz8UC) for instance.

### CLI

```
java -jar assert-fixer.jar (-c|--classpath) <classpath> (-t|--test-class) <testClass> (-m|--test-method) <testMethod> [(-s|--source-path) <sourcePath>] [(-p|--test-path) <testPath>] [--verbose] [(-o|--output) <output>] [-h|--help]
                          
    (-c|--classpath) <classpath>
          [Mandatory] Use must specify a complete classpath to execute tests on
          your project.
          The classpath should be formatted according to the OS.
  
    (-t|--test-class) <testClass>
          [Mandatory] Use must specify a failing test class.
          You must provide a full qualified name such as:
          my.package.example.ClassTest
  
    (-m|--test-method) <testMethod>
          [Mandatory] Use must specify at least one failing test method.
          Separate multiple values with ":" such as: test1:test2
  
    [(-s|--source-path) <sourcePath>]
          [Optional] Specify the path to the source folder.
          Separate multiple values with ":" such as: path/one:path/two/
  
    [(-p|--test-path) <testPath>]
          Specify the path to the test source folder.
          Separate multiple values with ":" such as: path/one:path/two/ (default:
          src/test/java/)
  
    [--verbose]
          Enable verbose mode
  
    [(-o|--output) <output>]
          [Optional] Specify an output folder for result and temporary files.
          (default: target/assert-fixer)
  
    [-h|--help]
          Display this help and exit

```
