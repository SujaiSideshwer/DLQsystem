import java.util.spi.ToolProvider;

public class Build {
    public static void main(String[] args) {
        var javac = ToolProvider.findFirst("javac")
                .orElseThrow(() -> new RuntimeException("javac tool not available"));
        int rc = javac.run(System.out, System.err, args);
        System.exit(rc);
    }
}
