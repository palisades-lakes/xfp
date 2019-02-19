package xfp.java;

import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.logging.Level;

import xfp.java.Log;

/** Java log libraries are too complicated...
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2018-06-26
 */

public final class Log {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final Level _level;
  public final Level level () { return _level; }

  private final PrintStream _stream;
  public final PrintStream stream () { return _stream; }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  
  public final void all (final Supplier s) {
    if (_level.intValue() <= Level.ALL.intValue()) {
      _stream.println((String) s.get()); } }
  
  public final void severe (final Supplier s) {
    if (_level.intValue() <= Level.SEVERE.intValue()) {
      _stream.println((String) s.get()); } }
  
  public final void warn (final Supplier s) {
    if (_level.intValue() <= Level.WARNING.intValue()) {
      _stream.println((String) s.get()); } }
  
  public final void info (final Supplier s) {
    if (_level.intValue() <= Level.INFO.intValue()) {
      _stream.println((String) s.get()); } }
  
  public final void info (final String s) {
    if (_level.intValue() <= Level.INFO.intValue()) {
      _stream.println(s); } }
  
  public final void debug (final Supplier s) {
    if (_level.intValue() <= Level.FINER.intValue()) {
      _stream.println((String) s.get()); } }
  
  public final void trace (final Supplier s) {
    if (_level.intValue() <= Level.FINEST.intValue()) {
      _stream.println((String) s.get()); } }
  
  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------
  
  @Override
  public final String toString () { 
    return "Log(" + _level + ")"; }
  
  //--------------------------------------------------------------
  // constructor
  //--------------------------------------------------------------

  private Log (final Level level,
               final PrintStream stream) { 
    assert null != level;
    assert null != stream;
    _level = level;
    _stream = stream; }

  public static final Log make (final Level level,
                                final PrintStream stream) {
    return new Log(level,stream); }

  public static final Log defaults () {
    return make(Level.INFO,System.out); }

  public static final Log fine () {
    return make(Level.FINE,System.out); }

  public static final Log debug () {
    return make(Level.FINER,System.out); }

  public static final Log trace () {
    return make(Level.FINEST,System.out); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
