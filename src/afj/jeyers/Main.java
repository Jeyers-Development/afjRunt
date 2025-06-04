package afj.jeyers;
import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import javax.swing.JOptionPane;
import java.util.*;
import java.io.InputStream;
import java.io.BufferedInputStream;

public class Main {
	public static final String col_RESET = "\u001B[0m";
	public static final String col_GREEN = "\u001B[32m";
	public static final String col_RED = "\u001B[31m";
	public static final String col_YELLOW = "\u001B[33m";
    
    static void intro() {
		System.out.println("afjRunt[address finder Java Runt] v1.1\nCreated by Jacob Meyers, Jeyers Development");
	}
    
    public static String pingRequest(int speed, String fullip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(fullip, port), speed);
            Thread t = new Thread(new Runnable(){
                public void run(){
                	PlayOrb();
                    JOptionPane.showMessageDialog(null, "Ip:? True: " + fullip+':'+port +"\nYou're welcome, you little runt.");
                }
            });
            t.start();
            return col_GREEN + "[true] " + fullip + ":" + port + col_RESET;
        } catch (Exception e) {
        	return col_RED + "[false] " + fullip + ":" + port + col_YELLOW + " \\ " + e + col_RESET;
        }
    }
	
    public static String pingRequestMC(int speed, String fullip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(fullip, port), speed);
            
            
            if (isMinecraftServer(socket, fullip, port)) {
                Thread t = new Thread(new Runnable(){
                    public void run(){
                        PlayOrb();
                        JOptionPane.showMessageDialog(null, "Minecraft server found at: " + fullip + ':' + port + "\nYou're welcome, you little runt.");
                    }
                });
                t.start();
                return col_GREEN + "[true] " + fullip + ":" + port + col_RESET;
            } else {
                return col_YELLOW + "[true, NOT minecraft] " + fullip + ":" + port + col_RESET;
            }

        } catch (Exception e) {
            return col_RED + "[false] " + fullip + ":" + port + col_YELLOW + " \\ " + e + col_RESET;
        }
    }
    
    private static boolean isMinecraftServer(Socket socket, String ip, int port) {
        try {
            socket.setSoTimeout(3000);
            
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(buf);

            handshake.writeByte(0x00);
            writeVarInt(handshake, 754); 
            writeVarInt(handshake, ip.length());
            handshake.writeBytes(ip);
            handshake.writeShort(port);
            writeVarInt(handshake, 1);
            
            writeVarInt(out, buf.size());
            out.write(buf.toByteArray());

            out.writeByte(0x01);
            out.writeByte(0x00);

            int size = readVarInt(in);

            if (size <= 0) return false;

            byte packetId = in.readByte();
            if (packetId != 0x00) return false;
            
            int stringLength = readVarInt(in);
            if (stringLength <= 0) return false;

            byte[] data = new byte[stringLength];
            in.readFully(data);

            String json = new String(data, "UTF-8");
            return json.contains("version") && json.contains("players");

        } catch (Exception e) {
            return false;
        }
    }

    private static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & ~0x7F) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte((paramInt & 0x7F) | 0x80);
            paramInt >>>= 7;
        }
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;

        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;

            if (j > 5) throw new RuntimeException("VarInt too big");

            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }
    public static void PlayOrb() {
    	try {
    		InputStream orbPath = new BufferedInputStream(Main.class.getResourceAsStream("orb.wav"));
    		if(orbPath!=null) {
    			AudioInputStream audioInput = AudioSystem.getAudioInputStream(orbPath);
    			Clip clip = AudioSystem.getClip();
    			clip.open(audioInput);
    			clip.start();
    		}
    	}
    	catch(Exception e) {
    		System.out.println(e);
    	}
    }

	
	public static void main(String[] args) {
		intro();
		Scanner scn = new Scanner(System.in);
		System.out.print("Enter Port (suggested:25565): ");
		int port = scn.nextInt();
		System.out.print("Speed ms (suggested:100): ");
		int speed = scn.nextInt();
		System.out.print("Number Type (0[incremental]/1[random]): ");
		int type = scn.nextInt();
		System.out.print("Searching For Minecraft Server? (Y/n): ");
		String minecraftYN = scn.next();
		System.out.print("WARNING ANY SUCCESFUL TESTS, THE OWNERS OF THOSE IPS WILL PROBABLY HAVE FULL ACCESS TO SEE YOUR IP (continue Y/n): ");
		String confirm = scn.next();
		scn.close();
		
		if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("yes")) { System.exit(0); }

		speed = Math.clamp(speed, 1, 1000000);
		
		List<Integer> ip = new ArrayList<>();
		ip.add(1);
		ip.add(1);
		ip.add(1);
		ip.add(1);

		
	    while (true){
	    	if (type == 0) {
	    	    ip.set(3,ip.get(3)+1);
	    	    if (ip.get(3) > 255) {
	    	    	ip.set(3,1);
	    	    	ip.set(2,ip.get(2)+1);
	    	        if (ip.get(2) > 255) {
	    	            ip.set(2, 1);
	    	            ip.set(1, ip.get(1)+1);
	    	            if (ip.get(1) > 255) {
	    	                ip.set(1,1);
	    	                ip.set(0, ip.get(0));
	    	                if (ip.get(0) > 255) {
	    	                    System.exit(0);
	    	                }
	    	            }
	    	        }
	    	    }
	    	}
	    	else {
	    		for (int i = 0; i < ip.size(); i++) {
	    			ip.set(i, (int)(Math.random()*254 + 1));
	    		}
	    	}
			
			String ipAddress = Integer.toString(ip.get(0))+'.' + Integer.toString(ip.get(1))+'.' + Integer.toString(ip.get(2))+'.' + Integer.toString(ip.get(3));
			if (minecraftYN.equalsIgnoreCase("Y") || minecraftYN.equalsIgnoreCase("yes")) { System.out.println(pingRequestMC(speed, ipAddress, port)); }
			else { System.out.println(pingRequest(speed, ipAddress, port)); }
	    }
	}
}
    
