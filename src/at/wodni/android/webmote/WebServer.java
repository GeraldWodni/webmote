package at.wodni.android.webmote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.impl.conn.DefaultResponseParser;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;
import android.util.Log;

public class WebServer implements Runnable {
	
	public static final int PORT = 8080;
	
	private Context mContext;
	private BasicHttpProcessor mHttpProcessor;
	private HttpService mHttpService;
	private BasicHttpContext mHttpContext;
	private HttpRequestHandlerRegistry  mHandlerRegistry;
	private ServerSocket mServerSocket;
	
	public WebServer(Context context) {
		mContext = context;
		
		mHttpContext = new BasicHttpContext();
		
		/* setup bloatware */
		mHttpProcessor = new BasicHttpProcessor();
		mHttpProcessor.addInterceptor(new ResponseDate());
		mHttpProcessor.addInterceptor(new ResponseServer());
		mHttpProcessor.addInterceptor(new ResponseContent());
		mHttpProcessor.addInterceptor(new ResponseConnControl());
		
		mHttpService = new HttpService(mHttpProcessor, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory() );
		
		mHandlerRegistry = new HttpRequestHandlerRegistry();
		mHandlerRegistry.register("/home", new HttpRequestHandler() {
			
			@Override
			public void handle(HttpRequest request, HttpResponse response,
					HttpContext context) throws HttpException, IOException {
				
				response.setEntity( new EntityTemplate(new ContentProducer() {
					@Override
					public void writeTo(OutputStream outstream) throws IOException {
						OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
						writer.write("HALLO DU!");
						writer.flush();
					}
				}) );
			}
		});
		
		mHttpService.setHandlerResolver(mHandlerRegistry);
	}

	@Override
	public void run() {
		try {
			mServerSocket = new ServerSocket(PORT);
			mServerSocket.setReuseAddress(true);
			
			while( !Thread.currentThread().isInterrupted() ) {
				final Socket socket = mServerSocket.accept();
				
				DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
				serverConnection.bind(socket, new BasicHttpParams());
				
				mHttpService.handleRequest(serverConnection, mHttpContext);
			}
		//}catch(InterruptedException e) {
		//	Log.e(WebService.LOG_PREFIX, e.toString());
		}catch(IOException e) {
			Log.e(WebService.LOG_PREFIX, e.toString());
		} catch (HttpException e) {
			Log.e(WebService.LOG_PREFIX, e.toString());
		}
		finally{
			if(mServerSocket != null && !mServerSocket.isClosed())
				try {
					mServerSocket.close();
				} catch (IOException e) {
					Log.e(WebService.LOG_PREFIX, e.toString());
				}
		}
	}
}
