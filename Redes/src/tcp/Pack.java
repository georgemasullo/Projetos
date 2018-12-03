package tcp;

import pacote.Pacote;

public class Pack {
	
	private Pacote pack;
	private boolean ack;
	private boolean enviar;
	
	
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

	public boolean isEnviar() {
		return enviar;
	}

	public void setEnviar(boolean enviar) {
		this.enviar = enviar;
	}
	
	

}
