import java.util.*;
public class StateRecorder {
	public Stack<String> SaveState = new  Stack<String>();
	public int SaveIndex ;
	public boolean SaveBool1 ;
	public boolean SaveBool2 ;
	public boolean SaveBool3 ;
	StateRecorder(Stack temp, int currIndex, boolean makeBranch1, boolean makeBranch2, boolean makeBranch3){
		SaveState = (Stack)temp.clone();
		SaveIndex = currIndex;
		SaveBool1 = makeBranch1;
		SaveBool2 = makeBranch2;
		SaveBool3 = makeBranch3;
	}
	StateRecorder(){	
	}
}
//(( (22 +5) * 23 *(-1) )  ) - 2 * 5 * 78 - 90 *(56 -6.90--89)
//((78)-(-890)+90)+9*78-90--98.567+(8-90)