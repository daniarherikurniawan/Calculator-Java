import java.util.Vector;

public class FirstTuple {
	public String Input;
	public char Elmt1;
	public char Elmt2;
	public char Elmt3;
	public Vector <String> TupleOne = new Vector();
	
	public void parseInputtoElmts(){
		Elmt1 = Input.charAt(2);
		Elmt2 = Input.charAt(4);
		Elmt3 = Input.charAt(6);
	}
}
