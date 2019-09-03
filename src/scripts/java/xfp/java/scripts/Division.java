package xfp.java.scripts;

import java.math.BigInteger;
import java.util.List;

import xfp.java.numbers.Natural;
import xfp.java.numbers.NaturalBEI0;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

/** Compare BigInteger and Natural results.
 * At least 4*2<sup>32</sup> tests to get calls to Natural.divadd.
 *
 * <pre>
 * j --source 12 -ea src/scripts/java/xfp/java/scripts/Division.java > division.txt
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-09-03
 */
@SuppressWarnings("unchecked")
public final class Division {

  private static final void
  divideAndRemainder (final BigInteger x0,
                      final BigInteger x1) {
    if (0 != x1.signum()) {
      final NaturalBEI0 y0 = NaturalBEI0.valueOf(x0);
      final NaturalBEI0 y1 = NaturalBEI0.valueOf(x1);
      final BigInteger[] xqr = x0.divideAndRemainder(x1);
      final BigInteger xq = xqr[0];
      final BigInteger xr = xqr[1];
      final List<Natural> yqr = y0.divideAndRemainder(y1);
      final BigInteger yq = ((NaturalBEI0)yqr.get(0)).bigIntegerValue();
      final BigInteger yr = ((NaturalBEI0)yqr.get(1)).bigIntegerValue();

      assert xq.equals(yq) :
        "\nquotients differ!"
      + "\n" + x0.toString(0x10)
      + "\n / "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + xq.toString(0x10)
      + "\n" + y0.toString()
      + "\n / "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + yq.toString(0x10);

      assert xr.equals(yr) :
        "\nremainders differ!"
      + "\n" + x0.toString(0x10)
      + "\n / "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + xr.toString(0x10)
      + "\n" + y0.toString()
      + "\n / "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + yr.toString(0x10); } }

  //--------------------------------------------------------------

  private static final long TRYS = (1L << 32);
  
  public static final void main (final String[] args) {
    final Generator gn =
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (long i=0;i<TRYS;i++) {
      if (0L==(i%(1L<<20))) { System.out.println(i); }
      divideAndRemainder(
        ((BigInteger) gn.next()).abs(),
        ((BigInteger) gn.next()).abs()); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
