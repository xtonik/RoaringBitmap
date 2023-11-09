package org.roaringbitmap;

import org.roaringbitmap.buffer.MappeableContainer;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class UnknownContainer extends Container {

  @Override
  public Container add(int begin, int end) {
    return null;
  }

  @Override
  public Container add(char x) {
    return null;
  }

  @Override
  public Container and(ArrayContainer x) {
    return null;
  }

  @Override
  public Container and(BitmapContainer x) {
    return null;
  }

  @Override
  public Container and(RunContainer x) {
    return null;
  }

  @Override
  protected int andCardinality(ArrayContainer x) {
    return 0;
  }

  @Override
  protected int andCardinality(BitmapContainer x) {
    return 0;
  }

  @Override
  protected int andCardinality(RunContainer x) {
    return 0;
  }

  @Override
  public Container andNot(ArrayContainer x) {
    return null;
  }

  @Override
  public Container andNot(BitmapContainer x) {
    return null;
  }

  @Override
  public Container andNot(RunContainer x) {
    return null;
  }

  @Override
  public void clear() {

  }

  @Override
  public Container clone() {
    return null;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isFull() {
    return false;
  }

  @Override
  public boolean contains(char x) {
    return false;
  }

  @Override
  public boolean contains(int minimum, int supremum) {
    return false;
  }

  @Override
  protected boolean contains(RunContainer runContainer) {
    return false;
  }

  @Override
  protected boolean contains(ArrayContainer arrayContainer) {
    return false;
  }

  @Override
  protected boolean contains(BitmapContainer bitmapContainer) {
    return false;
  }

  @Override
  public void deserialize(DataInput in) throws IOException {

  }

  @Override
  public void fillLeastSignificant16bits(int[] x, int i, int mask) {

  }

  @Override
  public Container flip(char x) {
    return null;
  }

  @Override
  public int getArraySizeInBytes() {
    return 0;
  }

  @Override
  public int getCardinality() {
    return 0;
  }

  @Override
  public void forEach(char msb, IntConsumer ic) {

  }

  @Override
  public void forAll(int offset, RelativeRangeConsumer rrc) {

  }

  @Override
  public void forAllFrom(char startValue, RelativeRangeConsumer rrc) {

  }

  @Override
  public void forAllUntil(int offset, char endValue, RelativeRangeConsumer rrc) {

  }

  @Override
  public void forAllInRange(char startValue, char endValue, RelativeRangeConsumer rrc) {

  }

  @Override
  public PeekableCharIterator getReverseCharIterator() {
    return null;
  }

  @Override
  public PeekableCharIterator getCharIterator() {
    return null;
  }

  @Override
  public PeekableCharRankIterator getCharRankIterator() {
    return null;
  }

  @Override
  public ContainerBatchIterator getBatchIterator() {
    return null;
  }

  @Override
  public int getSizeInBytes() {
    return 0;
  }

  @Override
  public Container iadd(int begin, int end) {
    return null;
  }

  @Override
  public Container iand(ArrayContainer x) {
    return null;
  }

  @Override
  public Container iand(BitmapContainer x) {
    return null;
  }

  @Override
  public Container iand(RunContainer x) {
    return null;
  }

  @Override
  public Container iandNot(ArrayContainer x) {
    return null;
  }

  @Override
  public Container iandNot(BitmapContainer x) {
    return null;
  }

  @Override
  public Container iandNot(RunContainer x) {
    return null;
  }

  @Override
  public Container inot(int rangeStart, int rangeEnd) {
    return null;
  }

  @Override
  public boolean intersects(ArrayContainer x) {
    return false;
  }

  @Override
  public boolean intersects(BitmapContainer x) {
    return false;
  }

  @Override
  public boolean intersects(RunContainer x) {
    return false;
  }

  @Override
  public boolean intersects(int minimum, int supremum) {
    return false;
  }

  @Override
  public Container ior(ArrayContainer x) {
    return null;
  }

  @Override
  public Container ior(BitmapContainer x) {
    return null;
  }

  @Override
  public Container ior(RunContainer x) {
    return null;
  }

  @Override
  public Container iremove(int begin, int end) {
    return null;
  }

  @Override
  public Container ixor(ArrayContainer x) {
    return null;
  }

  @Override
  public Container ixor(BitmapContainer x) {
    return null;
  }

  @Override
  public Container ixor(RunContainer x) {
    return null;
  }

  @Override
  public Container limit(int maxcardinality) {
    return null;
  }

  @Override
  public Container not(int rangeStart, int rangeEnd) {
    return null;
  }

  @Override
  int numberOfRuns() {
    return 0;
  }

  @Override
  public Container or(ArrayContainer x) {
    return null;
  }

  @Override
  public Container or(BitmapContainer x) {
    return null;
  }

  @Override
  public Container or(RunContainer x) {
    return null;
  }

  @Override
  public int rank(char lowbits) {
    return 0;
  }

  @Override
  public Container remove(int begin, int end) {
    return null;
  }

  @Override
  public Container remove(char x) {
    return null;
  }

  @Override
  public Container repairAfterLazy() {
    return null;
  }

  @Override
  public Container runOptimize() {
    return null;
  }

  @Override
  public char select(int j) {
    return 0;
  }

  @Override
  public void serialize(DataOutput out) throws IOException {

  }

  @Override
  public int serializedSizeInBytes() {
    return 0;
  }

  @Override
  public MappeableContainer toMappeableContainer() {
    return null;
  }

  @Override
  public void trim() {

  }

  @Override
  public void writeArray(DataOutput out) throws IOException {

  }

  @Override
  public void writeArray(ByteBuffer buffer) {

  }

  @Override
  public Container xor(ArrayContainer x) {
    return null;
  }

  @Override
  public Container xor(BitmapContainer x) {
    return null;
  }

  @Override
  public Container xor(RunContainer x) {
    return null;
  }

  @Override
  public BitmapContainer toBitmapContainer() {
    return null;
  }

  @Override
  public int nextValue(char fromValue) {
    return 0;
  }

  @Override
  public int previousValue(char fromValue) {
    return 0;
  }

  @Override
  public int nextAbsentValue(char fromValue) {
    return 0;
  }

  @Override
  public int previousAbsentValue(char fromValue) {
    return 0;
  }

  @Override
  public int first() {
    return 0;
  }

  @Override
  public int last() {
    return 0;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {

  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

  }

  @Override
  public Iterator<Character> iterator() {
    return null;
  }
}
