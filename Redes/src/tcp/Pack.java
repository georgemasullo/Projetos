package tcp;

import pacote.Pacote;

public class Pack {
	
	private Pacote pack;
	private boolean ack;
	
	public Pacote getPack() {
		return pack;
	}
	
	public void setPack(Pacote pack) {
		this.pack = pack;
	}
	
	public boolean isAck() {
		return ack;
	}
	
	public void setAck(boolean ack) {
		this.ack = ack;
	}
	
	

}
