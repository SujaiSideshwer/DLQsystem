import com.dlqsetup.mainentities.Message;
import com.dlqsetup.mainentities.MessageHandler;

public class ProcessMessageCommand<T> implements Command {
    private final MessageHandler<T> handler;
    private final Message<T> message;

    public ProcessMessageCommand(MessageHandler<T> handler, Message<T> message) {
        this.handler = handler;
        this.message = message;
    }

    @Override
    public void execute() throws Exception {
        handler.handle(message);
    }

    public Message<T> message(){
        return message;
    }
}
