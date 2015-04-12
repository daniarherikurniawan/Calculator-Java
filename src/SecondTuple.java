import java.util.Vector;

public class SecondTuple {
	public String Input;
	public char Elmt1;
	public String Elmt2;
	public Vector <String> TupleTwo = new Vector();
	
	public void parseInputtoElmts(){
		int idxComma = 0;
		while(Input.charAt(idxComma)!=',')
			idxComma++;
		
		int idxBracket = Input.length()-1;
		while(Input.charAt(idxBracket)!=')')
			idxBracket--;
		Elmt1 = Input.charAt(2);
		Elmt2 = Input.substring(idxComma+1,idxBracket);
	}
	public boolean IsElmt2Epsilon(){
		return Elmt2.compareTo("e")==0;
	}
}
