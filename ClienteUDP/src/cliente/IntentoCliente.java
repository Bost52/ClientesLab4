package cliente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

public class IntentoCliente {

	final static int PORT_SERVIDOR = 8888;
	public static byte[] HASH_RECIBIDO;
	public static int TAMANO_ULTIMO;
	public static Integer NUMERO;

	public static long inicioConexion;
	public static long inicioTransferenciaDatos;
	public static long finTransferenciaDatos;
	public static long inicioTransferenciaPaquetes;
	public static long finTransferenciaPaquetes;
	public static long inicioUnionDeBytes;
	public static long finUnionDeBytes;
	public static long inicioCalcularHash;
	public static long finCalcularHash;
	public static long inicioCreacionArchivo;
	public static long finCreacionArchivo;
	public static long finConexion;

	public static int paquetesTransmitidos = 0;
	public static int paquetesEnviados = 0;


	public static void main(String[] args) {

		try {
			//Envio el mensaje de listo al servidor
			InetAddress address = InetAddress.getByName("157.253.205.67");
			String mensajeIni = "Listo";
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket paqueteIni = new DatagramPacket(mensajeIni.getBytes(), mensajeIni.getBytes().length, address, PORT_SERVIDOR);
			socket.send(paqueteIni);
			paquetesEnviados++;


			inicioConexion = System.currentTimeMillis();

			//Recibo el video
			byte[] buf = new byte[61440];
			DatagramPacket paquete = new DatagramPacket(buf, buf.length);
			socket.receive(paquete);
			
			inicioTransferenciaDatos = System.currentTimeMillis();

			//Cuento paquetes y hash
			String contador = new String(paquete.getData());
			NUMERO = Integer.valueOf(contador.trim());
			System.out.println("Num paquetes: " + NUMERO);

			byte[] bytesult = new byte[61440];
			DatagramPacket paqueteUlt = new DatagramPacket(bytesult, bytesult.length);
			socket.receive(paqueteUlt);
			String tam = new String(paqueteUlt.getData());
			TAMANO_ULTIMO = Integer.valueOf(tam.trim());
			System.out.println("Tamanio Ultimo paquete: " + TAMANO_ULTIMO);

			//Recibo el hash a validar
			buf = new byte[20];
			paquete = new DatagramPacket(buf, buf.length);
			socket.receive(paquete);
			HASH_RECIBIDO = paquete.getData();
			System.out.println("Hash recibido: " + HASH_RECIBIDO);

			finTransferenciaDatos = System.currentTimeMillis();


			inicioTransferenciaPaquetes = System.currentTimeMillis();
			DatagramPacket packet;
			byte[] archivoOriginal = new byte[0];
			ArrayList<byte[]> arreglo = new ArrayList<>();
			System.out.println("Empezare a recibir");
			for (int i = 0; i < NUMERO-1; i++) {
				byte[] buffer = new byte[61440];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				arreglo.add(packet.getData());
				paquetesTransmitidos++;
				if(i%100==0)
					System.out.println(NUMERO + " " + i);
			}

			byte[] buffer = new byte[TAMANO_ULTIMO];
			packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			arreglo.add(packet.getData());
			paquetesTransmitidos++;
			System.out.println("Ultimo recibido");
			finTransferenciaPaquetes = System.currentTimeMillis();

			//Union de paquetes
			inicioUnionDeBytes = System.currentTimeMillis();
			int yy = 0;
			for(byte[] x: arreglo)
			{
				archivoOriginal = (byte[])ArrayUtils.addAll(archivoOriginal, x);
				if(yy%100==0)
					System.out.println(yy);
				yy++;
			}
			System.out.println("fin union paquetes");
			finUnionDeBytes = System.currentTimeMillis();

			//Calculo de hash
			inicioCalcularHash = System.currentTimeMillis();

			byte[] hash2 = new byte[1];
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			hash2 = md.digest(archivoOriginal);

			String hash1 = new String(hash2);
			String hashre = new String(HASH_RECIBIDO);
			System.out.println("fin hash");
			finCalcularHash = System.currentTimeMillis();

			//Creacion del file
			inicioCreacionArchivo = System.currentTimeMillis();

			try (FileOutputStream fos = new FileOutputStream("/home/s2g2/ClientesLab4/ClienteUDP/videocreado.mp4")) {
				fos.write(archivoOriginal);
			}
			catch(Exception e)
			{

			}
			System.out.println("fin creacion");
			finCreacionArchivo = System.currentTimeMillis();

			//Envio respuesta de recibido, generalmente sera de error ya que seguramente llego incompleto el archivo.
			String resp;
			if(hash1.trim().equals(hashre.trim()))
			{
				resp = "correcto";
				buf = resp.getBytes();
				paquete = new DatagramPacket(buf, buf.length, address, PORT_SERVIDOR);
				socket.send(paquete);
				paquetesEnviados++;
				//System.out.println("correcto");
				finConexion = System.currentTimeMillis();
			}
			else
			{
				resp = "error";
				buf = resp.getBytes();
				paquete = new DatagramPacket(buf, buf.length, address, PORT_SERVIDOR);
				socket.send(paquete);
				paquetesEnviados++;
				System.out.println("incorrecto");
				finConexion = System.currentTimeMillis();
			}

			//Escribo en log todo lo obtenido.
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss:SS");    
			BufferedWriter writer = new BufferedWriter(new FileWriter("/home/s2g2/ClientesLab4/ClienteUDP/",true));

			writer.write("Inicio prueba: ");
			writer.newLine();
			Date resultdate = new Date(inicioConexion);
			writer.write(sdf.format(resultdate));
			writer.newLine();
			String nombre;
			String tamano;
			if(NUMERO==1)
			{
				nombre = "video1";
				tamano = "tamaño en bytes: " + archivoOriginal.length;
			}
			else
			{
				nombre = "video2";
				tamano = "tamaño en bytes: " + archivoOriginal.length;
			}
			writer.write("Nombre archivo: ");
			writer.newLine();
			writer.write(nombre);
			writer.newLine();
			writer.write(tamano);
			writer.newLine();
			writer.write("IP: ");
			writer.newLine();
			writer.write(InetAddress.getLocalHost().getHostAddress());
			writer.newLine();
			writer.write("Se realizó con éxito el hash: ");
			writer.newLine();
			writer.write(resp);
			writer.newLine();

			writer.write("Inicio Transferencia datos: ");
			Date resultdate2 = new Date(inicioTransferenciaDatos);
			writer.write(sdf.format(resultdate2));
			writer.newLine();

			writer.write("Fin Transferencia datos: ");
			Date resultdate3 = new Date(finTransferenciaDatos);
			writer.write(sdf.format(resultdate3));
			writer.newLine();

			writer.write("Inicio Transferencia paquetes: ");
			Date resultdate4 = new Date(inicioTransferenciaPaquetes);
			writer.write(sdf.format(resultdate4));
			writer.newLine();

			writer.write("Fin Transferencia paquetes: ");
			Date resultdate5 = new Date(finTransferenciaPaquetes);
			writer.write(sdf.format(resultdate5));
			writer.newLine();

			writer.write("Inicio Union bytes: ");
			Date resultdate6 = new Date(inicioUnionDeBytes);
			writer.write(sdf.format(resultdate6));
			writer.newLine();

			writer.write("Fin Union bytes: ");
			Date resultdate7 = new Date(finUnionDeBytes);
			writer.write(sdf.format(resultdate7));
			writer.newLine();

			writer.write("Inicio Calcular hash: ");
			Date resultdate8 = new Date(inicioCalcularHash);
			writer.write(sdf.format(resultdate8));
			writer.newLine();

			writer.write("Fin Calcular hash: ");
			Date resultdate9 = new Date(finCalcularHash);
			writer.write(sdf.format(resultdate9));
			writer.newLine();

			writer.write("Inicio Creacion archivo: ");
			Date resultdate10 = new Date(inicioCreacionArchivo);
			writer.write(sdf.format(resultdate10));
			writer.newLine();

			writer.write("Fin Creacion archivo: ");
			Date resultdate11 = new Date(finCreacionArchivo);
			writer.write(sdf.format(resultdate11));
			writer.newLine();

			writer.write("Fin Conexión: ");
			Date resultdate12 = new Date(finCreacionArchivo);
			writer.write(sdf.format(resultdate12));
			writer.newLine();

			writer.write("Numero esperado de paquetes recibidos: ");
			writer.newLine();
			writer.write("" + NUMERO);
			writer.newLine();
			writer.write("Paquetes recibidos: ");
			writer.newLine();
			writer.write("" + paquetesTransmitidos);
			writer.newLine();
			writer.write("Paquetes enviados: ");
			writer.newLine();
			writer.write("" + paquetesEnviados);
			writer.newLine();

			writer.close();

			socket.close();


		}catch(Exception e)
		{
			System.out.println("Algo sucedió" + e.getLocalizedMessage());
		}
		System.out.println("Fin cliente");
	}
}
