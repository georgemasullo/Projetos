package pacote;

public class Pacote {
	private int sequenceNumber;
	private int ackNumber;
	private short ConnectionID;
	private short ASF;
	private byte Dados[];
	
	public boolean getS() {
		if (ASF == 2 || ASF == 6 || ASF == 3 || ASF == 7) {
			return true;
		}
		return false;
	}
	public short getASF() {
		return ASF;
	}
	
	public void SetS(boolean b) {
		if ((b == true && getS() == true) || (b == false && getS() == false)) {
			return;
		}else if (b== true && getS() == false){
			if (ASF == 0) {
				ASF = 2;
			} else if (ASF == 1) {
				ASF = 3;
			} else if (ASF == 4) {
				ASF = 6;
			} else {
				ASF = 7;
			}
		}else{
			if (ASF == 2) {
				ASF = 0;
			} else if (ASF == 3) {
				ASF = 1;
			} else if (ASF == 6) {
				ASF = 4;
			} else {
				ASF = 5;
			}
		}
	}
	
	public boolean getA(){
		if (ASF == 4 || ASF == 5 || ASF == 6 || ASF == 7) {
			return true;
		}
		return false;
	}
	
	public void SetA(boolean b) {
		if ((b == true && getS() == true) || (b == false && getS() == false)) {
			return;
		}else if (b== true && getS() == false){
			if (ASF == 0) {
				ASF = 4;
			} else if (ASF == 1) {
				ASF = 5;
			} else if (ASF == 2) {
				ASF = 6;
			} else {
				ASF = 7;
			}
		}else{
			if (ASF == 4) {
				ASF = 0;
			} else if (ASF == 5) {
				ASF = 1;
			} else if (ASF == 6) {
				ASF = 2;
			} else {
				ASF = 3;
			}
		}
	}
	public boolean GetF(){
		if (ASF == 1 || ASF == 5 || ASF == 3 || ASF == 7) {
			return true;
		}
		return false;
	}
	public void SetF(boolean b) {
		if ((b == true && getS() == true) || (b == false && getS() == false)) {
			return;
		}else if (b== true && getS() == false){
			if (ASF == 0) {
				ASF = 1;
			} else if (ASF == 2) {
				ASF = 3;
			} else if (ASF == 4) {
				ASF = 5;
			} else {
				ASF = 7;
			}
		}else {
			if (ASF == 1) {
				ASF = 0;
			} else if (ASF == 3) {
				ASF = 2;
			} else if (ASF == 5) {
				ASF = 4;
			} else {
				ASF = 6;
			}
		}
	}
}
