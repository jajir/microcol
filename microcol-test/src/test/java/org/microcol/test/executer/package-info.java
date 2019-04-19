/**
 * This package just call main test cases. It;s work around of nasty bug in
 * surefire plugin. When test class contain any method with parameter or
 * returning non-null value than whole class is not skipped and any test within
 * class is executed.
 */
package org.microcol.test.executer;
