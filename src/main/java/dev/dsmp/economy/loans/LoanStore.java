package dev.dsmp.economy.loans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.io.IOException;

public final class LoanStore {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    public LoanStore(Path directory, String scope) {
        if (scope == null || scope.isBlank()) throw new IllegalArgumentException("No active server/account");
        try { path = directory.resolve(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(scope.getBytes(StandardCharsets.UTF_8))) + ".json"); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    public LoanBook load() throws IOException {
        if (!Files.exists(path)) return new LoanBook();
        if (Files.size(path) > 2_000_000) throw new IOException("Loan file exceeds 2 MB");
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return LoanBook.restore(JSON.fromJson(reader, LoanBook.Snapshot.class));
        } catch (IllegalArgumentException ex) { throw new IOException("Invalid loan file; original preserved", ex); }
    }
    public void save(LoanBook book) throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "loans-", ".tmp");
        try {
            Files.writeString(temporary, JSON.toJson(book.snapshot()), StandardCharsets.UTF_8);
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
