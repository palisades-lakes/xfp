package xfp.java.prng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.rng.simple.internal.SeedFactory;

import xfp.java.exceptions.Exceptions;

/** Seeds for pseduo-random number generators.
 *
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-05
 */

public final class Seeds {

  //--------------------------------------------------------------
  // generate seeds
  //--------------------------------------------------------------
  /** Get a default seed from commons rng 
   * <code>SeedFactory</code>.
   */

  public static final int[] commonsRngSeed (final int size) {
    return SeedFactory.createIntArray(size); }

  //--------------------------------------------------------------
  // Note: restoring these method requires adding the 
  // uncommons maths dependency to the pom
  // and the necessary imports to this file.
  //--------------------------------------------------------------
  // TODO: eliminate dependency on uncommons math?
  /** Get a default seed from uncommons math.
   */

  //  public static final int[] defaultSeed (final int size) {
  //    return
  //      (new ByteArray2IntArray())
  //      .convert(
  //        DefaultSeedGenerator
  //        .getInstance()
  //        .generateSeed(4 * size)); }

  //--------------------------------------------------------------
  // TODO: eliminate dependency on uncommons math?
  /** Get a seed from random.org.
   */

  //  public static final int[] randomDotOrgSeed (final int size) {
  //    try {
  //      return
  //        (new ByteArray2IntArray())
  //        .convert(
  //          (new RandomDotOrgSeedGenerator())
  //          .generateSeed(4 * size)); }
  //    catch (final SeedException e) {
  //      e.printStackTrace();
  //      throw new RuntimeException(e); } }

  //--------------------------------------------------------------
  // re-use seeds via jarred resources
  //--------------------------------------------------------------

  public static final void write (final int[] seed,
                                  final File f) {
    f.getParentFile().mkdirs();
    try {
      final PrintWriter w = new PrintWriter(f);
      try { for (final int i : seed) { w.println(i); } }
      finally { w.close(); } }
    catch (final FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e); } }

  //--------------------------------------------------------------

  public static final int[] read (final File f) {
    try {
      final List<String> lines = Files.readAllLines(f.toPath());
      final int[] seed = new int[lines.size()];
      int i = 0;
      for (final String line : lines) {
        seed[i++] = Integer.parseInt(line); }  
      return seed; }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e); } }

  //--------------------------------------------------------------
  /** Read an <code>int[]</code> from <code>url</code>,
   * assuming each line holds an <code>int</code>.
   */

  private static final int[] seed (final URL url) {
    try {
      final BufferedReader r = 
        new BufferedReader(
          new InputStreamReader(
            url.openStream()));
      try {
        final Object[] lines = r.lines().toArray();
        final int n = lines.length;
        final int[] seed = new int[n];
        for (int i = 0;i<n;i++) {
          seed[i] = Integer.parseInt((String) lines[i]); }  
        return seed; }
      finally { r.close(); } }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e); } }

  //--------------------------------------------------------------
  /** If <code>r</code> is a String, coerce it to a URL,
   * and get the seed from there.
   */

  public static final int[] seed (final Object r) {

    if (r instanceof URL) { return seed((URL) r); }

    if (r instanceof String) {
      final URL url = 
        Thread.currentThread()
        .getContextClassLoader()
        .getResource((String) r);
      assert null != url : "no resource at " + r;
      return seed(url); }

    throw Exceptions.unsupportedOperation(null,"seed",r); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Seeds () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
