package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/**
 * A conversation in the notification API.
 *
 * @see <a href="https://developer.github.com/v3/activity/notifications/">documentation</a>
 * @see GHNotificationStream
 * @author Kohsuke Kawaguchi
 */
public class GHThread extends GHObject {
    private GitHub root;
    private GHRepository repository;
    private Subject subject;
    private String reason;
    private boolean unread;
    private String last_read_at;
    private String url,subscription_url;

    static class Subject {
        String title;
        String url;
        String latest_comment_url;
        String type;
    }

    private GHThread() {// no external construction allowed
    }

    /**
     * Returns null if the entire thread has never been read.
     */
    public Date getLastReadAt() {
        return GitHub.parseDate(last_read_at);
    }

    public String getReason() {
        return reason;
    }

    public GHRepository getRepository() {
        return repository;
    }

    // TODO: how to expose the subject?

    public boolean isRead() {
        return !unread;
    }

    public String getTitle() {
        return subject.title;
    }

    public String getType() {
        return subject.type;
    }

    /*package*/ GHThread wrap(GitHub root) {
        this.root = root;
        if (this.repository!=null)
            this.repository.wrap(root);
        return this;
    }

    /**
     * Marks this thread as read.
     */
    public void markAsRead() throws IOException {
        new Requester(root).method("PATCH").to(url);
    }

    /**
     * Subscribes to this conversation to get notifications.
     */
    public GHSubscription subscribe(boolean subscribed, boolean ignored) throws IOException {
        return new Requester(root)
            .with("subscribed", subscribed)
            .with("ignored", ignored)
            .method("PUT").to(subscription_url, GHSubscription.class).wrapUp(root);
    }

    /**
     * Returns the current subscription for this thread.
     *
     * @return null if no subscription exists.
     */
    public GHSubscription getSubscription() throws IOException {
        try {
            return new Requester(root).to(subscription_url, GHSubscription.class).wrapUp(root);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
