package expirationCleaner;

import java.util.ArrayList;
import java.util.List;

public class ExpirationCleaner extends Thread
{
    private final List<SubscriberTTL> subscriberTTLs = new ArrayList<>();
    private final int flushingFreq;

    public ExpirationCleaner() {this.flushingFreq = 1000;}
    public ExpirationCleaner(int flushingFreq) {this.flushingFreq = flushingFreq;}

    public void subscribe(SubscriberTTL s) {subscriberTTLs.add(s);}
    public void unSubscribe(SubscriberTTL s) {subscriberTTLs.remove(s);}

    public void notifyCleaning() {
        for (SubscriberTTL s: this.subscriberTTLs)
            s.updateExpiration();
    }

    @Override
    public void run(){
        while (true)
        {
            notifyCleaning();
            try {
                Thread.sleep(this.flushingFreq);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
