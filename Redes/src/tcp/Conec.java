package tcp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import pacote.Pacote;

public class Conec {
	private InetAddress endereco;
	private int porta;
	private int id;
	private boolean isClose;
	private boolean tempoAcabou;
	private Pacote ultimoComf;
	private BufferedWriter escrverAq;
	private boolean evento;
	
	public Conec() {
		// TODO Auto-generated constructor stub
		this.tempoAcabou=false;
	}
	
	public InetAddress getEndereco() {
		return endereco;
	}
	public void setEndereco(InetAddress endereco) {
		this.endereco = endereco;
	}
	public int getPorta() {
		return porta;
	}
	public void setPorta(int porta) {
		this.porta = porta;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isClose() {
		return isClose;
	}
	public void close() {
		try {
			this.escrverAq.close();
			this.isClose = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro o fechar arquivo "+this.id);
			e.printStackTrace();
		}
		
	}
	public boolean isTempoAcabou() {
		return tempoAcabou;
	}
	public void setTempoAcabou(boolean tempoAcabou) {
		this.tempoAcabou = tempoAcabou;
	}
	public Pacote getUltimoComf() {
		return ultimoComf;
	}
	public void setUltimoComf(Pacote ultimoComf) {
		this.evento=true;
		this.ultimoComf = ultimoComf;
	}
	public void tempo() {
		this.evento=false;
		 Runnable r = () -> {
			 ExecutorService es = Executors.newSingleThreadExecutor();

		        Callable<Void> c = () -> {
		        	 this.time();
					return null;
		        };
		        Future<Void> f = es.submit(c);
		        
		        try {
					f.get(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					this.escreverAq("Erro InterruptedException "+e.getMessage());
					this.close();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					this.escreverAq("Erro ExecutionException "+e.getMessage());
					this.close();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					this.tempoAcabou=true;
					this.escreverAq("Erro TimeoutException "+e.getMessage());
					this.close();
				}
	     };
	     Thread t = new Thread(r);
	     t.start();
	}
	private void time() {
		while(evento==false) {
			continue;
		}
		return;
	}
	public void setAq(File f) {
		try {
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			this.escrverAq = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro ao preparar aquivo para escrever");
			e.printStackTrace();
		}
		
	}
	public void escreverAq(String palavra) {
		try {
			this.escrverAq.write(palavra);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro ao escrver "+ this.id);
			e.printStackTrace();
		}
	}
	
	
}
