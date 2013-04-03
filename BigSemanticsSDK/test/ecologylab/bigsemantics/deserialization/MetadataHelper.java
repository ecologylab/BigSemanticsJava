package ecologylab.bigsemantics.deserialization;

import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.generic.Continuation;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;

public class MetadataHelper implements Continuation<DocumentClosure> {

	public static final int DEFAULT_WAIT_TIMEOUT = 60000;

	private Object lock = new Object();

	private Document downloadedDocument = null;

	public Document getDownloadedDocument() {
		return downloadedDocument;
	}

	private void waitForDownload(long timeout) {
		synchronized (lock) {
			try {
				lock.wait(timeout);
			} catch (InterruptedException e) {
				Debug.debugT(this, "waitForDownload() interrupted.");
				return;
			}
		}
		Debug.debugT(this, "waitForDownload() notified.");
	}

	@Override
	public void callback(DocumentClosure closure) {
		if (closure != null
				&& closure.getDownloadStatus() == DownloadStatus.DOWNLOAD_DONE) {
			downloadedDocument = closure.getDocument();
		}
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	static public Document downloadDocument(ParsedURL purl,
			SemanticsSessionScope sss) {
		Document doc = sss.getOrConstructDocument(purl);
		DocumentClosure closure = doc.getOrConstructClosure();
		MetadataHelper helper = new MetadataHelper();
		closure.addContinuation(helper);
		closure.queueDownload();
		helper.waitForDownload(DEFAULT_WAIT_TIMEOUT);
		return helper.getDownloadedDocument();
	}

}
