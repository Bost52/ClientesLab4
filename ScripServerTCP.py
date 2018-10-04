# import socket programming library 
# -*- coding: utf-8 -*-
import socket 
import hashlib
from datetime import datetime
# import thread module 

from threading import Thread
  
#print_lock = threading.Lock() 
#s = threading.Condition();
ClientCount = int(input("Especifique cuantos clientes quiere en simultaneo\n" ))
ConnectedClients = []
numClientes = 0

dd = open('250.webm','rb')
contenido1 = dd.read()
dd2 = open('500.mp4','rb')
contenido2 = dd2.read()
m1 = hashlib.sha256();
m1.update(contenido1)
dig1 = m1.digest()
m2 = hashlib.sha256();
m2.update(contenido2)
dig2 = m2.digest()

def millis(start_time):
    dt = datetime.now() - start_time
    ms = (dt.days * 24 * 60 * 60 + dt.seconds) * 1000 + dt.microseconds / 1000.0
    return ms
# thread fuction 
class Client(Thread): 
        def __init__(self,conn,addr):
            Thread.__init__(self)
            self.conn = conn
            self.addr = addr
        
        def run (self):
            logServer = open('logServer.txt','a')
            logServer.write('---------------------------------')
                # Tamanio del archivo
            data = self.conn.recv(2048)
            datoTamanio = data.decode('utf-8')
                     
                # reverse the given string from client 
            if datoTamanio == '250':
                #with open('250.webm','rb') as dd:
                self.conn.send(dig1)
                with open('250.webm','rb') as de:
                    start_time = datetime.now()
                    contenido = de.read(4096)
                    contador = 0
                    while contenido:
                        self.conn.send(contenido)
                        contenido = de.read(4096)
                        contador += 1
                logServer.write('Tiempo en enviar los paquetes\n')
                logServer.write(str(millis(start_time))) 
                logServer.write('\nCantidad de paquetes enviados\n')       
                logServer.write(str(contador))
            else:
                #with open('500.mp4','rb') as dd:
                self.conn.send(dig2)
                with open('500.mp4','rb') as de:
                    contenido = de.read(4096)
                    start_time = datetime.now()
                    contador = 0
                    while contenido:
                        self.conn.send(contenido)
                        contenido = de.read(4096)
                        contador +=1
		        logServer.write('Tiempo en enviar los paquetes\n')
		        logServer.write(str(millis(start_time)))
                logServer.write('\nCantidad de paquetes enviados\n')
                logServer.write(str(contador))
            print('En Cliente recibio el archivo correctamente')
            logServer.close()
            self.conn.close()             
    
def Main(): 
        global ConnectedCount
        global numClientes
        host = '' 
  
        # reverse a port on your computer 
        # in our case it is 12345 but it 
        # can be anything 
        port = 11000
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
        s.bind((host, port)) 
        #while True:    
            #print("socket binded to post", port) 
              
            # put the socket into listening mode 
        s.listen(25) 
        print("socket is listening") 
              
            # a forever loop until client wants to exit 
                
        while numClientes < ClientCount: 
              
                # establish connection with client 
            c, addr = s.accept() 
                # lock acquired by client 
                #print_lock.acquire() 
                #print('Connected to :', addr[0], ':', addr[1]) 
                    
                #esperar a recibir "Preparado para recibir"
            resp = c.recv(1024)
            if(resp.decode("utf-8") == "Preparado para recibir"):
                # create a new thread and add it to the created list  
                c = Client(c,addr)
                ConnectedClients.append(c)
                numClientes += 1
                        
                    
        print("Llegaron todos los clientes")
        for x in ConnectedClients:
            x.start()
        s.close() 
        
  
if __name__ == '__main__': 
    Main() 
