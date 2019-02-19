# xfp

Utilities for 'exact' floating point calculatation.
 
By 'exact' floating point operation, I mean a function that takes
<code>double</code>
(or <code>float</code>)
inputs and returns a <code>double</code> 
(or <code>float</code>)
value,
where the value is what you would get if you could convert all 
the <code>double</code> inputs to true real numbers,
do the calculation in real arithmetic,
and then round the real value to <code>double</code> at the end.
 
IEEE 754-1985 and <code>java.lang.Math</code> guarantee this for 
<code>+</code>, <code>-</code>, <code>*</code>, <code>/</code>, 
and <code>sqrt</code> (and <code>Math.fma</code>).
 
IEEE 754-2008 <em>recommends</em> it for a collection of 
elementary functions, in addition to the 5 basic operations.
<code>java.lang.Math</code> only offers within 1 <code>ulp</code> 
accuracy for those functions.)
 
Any non-trivial floating calculation will compose multiple basic 
operations, often returning a value far from the 'exact' ideal of
'real arithmetic rounded at the end'.
 
An easy, but expensive fix is to 
## Usage

### Dependency 

Available from (TODO repository).

Maven:

```xml
<dependency>
  <groupId>palisades-lakes</groupId>
  <artifactId>xfp</artifactId>
  <version>0.0.x</version>
</dependency>
```

### Code examples

  
## Acknowledgments

### ![Yourkit](https://www.yourkit.com/images/yklogo.png)

YourKit is kindly supporting open source projects with its full-featured Java
Profiler.

YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

* <a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
* <a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.

