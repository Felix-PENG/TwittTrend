package com.cc.twitttrend.servlet;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import com.cc.twitttrend.model.Notification;
import com.cc.twitttrend.wrapper.SNSWrapper;
/**
 * Servlet implementation class SNSServlet
 */
@WebServlet("/snssubscriber")
public class SNSSubscriber extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private static final Log log = LogFactory.getLog(Notification.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SNSSubscriber() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SecurityException{
		SNSWrapper snsWrapper = SNSWrapper.getSingleton();
		//Get the message type header.
		String messagetype = request.getHeader("x-amz-sns-message-type");
		//If message doesn't have the message type header, don't process it.
		if (messagetype == null)
			return;

		// Parse the JSON message in the message body
		// and hydrate a Message object with its contents 
		// so that we have easy access to the name/value pairs 
		// from the JSON message.
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(request.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		//Message msg = readMessageFromJson(builder.toString());
		Notification notification = new Notification(builder.toString());

		// The signature is based on SignatureVersion 1. 
		// If the sig version is something other than 1, 
		// throw an exception.
		if (notification.getSignatureVersion().equals("1")) {
			// Check the signature and throw an exception if the signature verification fails.
			if (isMessageSignatureValid(notification)){
				//log.info(">>Signature verification succeeded");
			}else {
				//log.info(">>Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		}
		else {
			//log.info(">>Unexpected signature version. Unable to verify signature.");
			throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		}

		// Process the message based on type.
		if (messagetype.equals("Notification")) {
			//TODO: Do something with the Message and Subject.
			//Just log the subject (if it exists) and the message.
//			String logMsgAndSubject = ">>Notification received from topic " + notification.getTopicArn();
//			if (notification.getSubject() != null)
//				logMsgAndSubject += " Subject: " + notification.getSubject();
//			logMsgAndSubject += " Message: " + notification.getMessage();
//			log.info(logMsgAndSubject);
			String message = notification.getMessage();
			WebServer.send(message);
		}
		else if (messagetype.equals("SubscriptionConfirmation"))
		{
			//TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics 
			//that you want to enable to add this endpoint as a subscription.

			//Confirm the subscription by going to the subscribeURL location 
			//and capture the return value (XML message body as a string)
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(new URL(notification.getSubscribeURL()).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			snsWrapper.confirmSubscribe(notification.getTopicArn(),notification.getToken());
			//log.info(">>Subscription confirmation (" + notification.getSubscribeURL() +") Return value: " + sb.toString());
			//TODO: Process the return value to ensure the endpoint is subscribed.
		}
		else if (messagetype.equals("UnsubscribeConfirmation")) {
			//TODO: Handle UnsubscribeConfirmation message. 
			//For example, take action if unsubscribing should not have occurred.
			//You can read the SubscribeURL from this message and 
			//re-subscribe the endpoint.
			//log.info(">>Unsubscribe confirmation: " + notification.getMessage());
		}
		else {
			//TODO: Handle unknown message type.
			//log.info(">>Unknown message type.");
		}
		//log.info(">>Done processing message: " + notification.getMessageId());
	}

	private static boolean isMessageSignatureValid(Notification notification) {
		try {
			URL url = new URL(notification.getSigningCertURL());
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(notification.getMessageBytesToSign());
			return sig.verify(Base64.decodeBase64(notification.getSignature()));
		}
		catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);
		}
	}
}
