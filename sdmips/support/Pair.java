package support;
public class Pair<U,V> {
  private final U fst;
  private final V snd;

  public U getFst() {
    return fst;
  }

  public V getSnd() {
    return snd;
  }

  public Pair(U fst, V snd) {
    this.fst = fst;
    this.snd = snd;
  }


  @Override
  public boolean equals(Object o){
	  System.out.println("eq");
	  if ( o instanceof Pair p){
	 	return this.fst.equals(p.getFst()) && this.snd.equals(p.getSnd());
	  }
	  return false;
  }
}
