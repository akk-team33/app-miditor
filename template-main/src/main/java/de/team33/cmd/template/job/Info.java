package de.team33.cmd.template.job;

import de.team33.cmd.template.runtime.BadConditionException;
import de.team33.cmd.template.runtime.Condition;
import de.team33.cmd.template.runtime.Output;
import de.team33.patterns.enums.pan.Values;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

class Info implements Runnable {

    static final String EXCERPT = "Get basic info about a file or directory.";

    private final Output out;
    private final Path path;

    private Info(final Output out, final Path path) {
        this.out = out;
        this.path = path.toAbsolutePath().normalize();
    }

    static Info job(final Condition condition) throws BadConditionException {
        return condition.limited(3)
                        .flatMap(cnd -> cnd.arg(2))
                        .map(Path::of)
                        .map(path -> new Info(condition.out(), path))
                        .orElseThrow(badCondition(condition));
    }

    private static Supplier<BadConditionException> badCondition(final Condition condition) {
        return () -> BadConditionException.format(Info.class, condition.cmdLine(), condition.cmdName());
    }

    @Override
    public final void run() {
        final FileInfo info = FileInfo.of(path);
        out.printf("Path ....... : %s%n", info.path);
        out.printf("type ....... : %s%n", info.type);
        out.printf("size ....... : %s%n", info.size);
        out.printf("creation ... : %s%n", info.creationTime());
        out.printf("lastAccess . : %s%n", info.lastAccessTime());
        out.printf("lastModified : %s%n", info.lastModifiedTime());
        out.printf("fileKey .... : %s%n", info.fileKey());
    }

    @SuppressWarnings("unused")
    private enum FileType {
        MISSING(Objects::isNull),
        NORMAL(BasicFileAttributes::isRegularFile),
        DIRECTORY(BasicFileAttributes::isDirectory),
        SYMBOLIC_LINK(BasicFileAttributes::isSymbolicLink),
        SPECIAL_FILE(BasicFileAttributes::isOther);

        static final Values<FileType> VALUES = Values.of(FileType.class);

        private final Predicate<BasicFileAttributes> filter;

        FileType(Predicate<BasicFileAttributes> filter) {
            this.filter = filter;
        }
    }

    private record FileInfo(Path path, FileType type, long size, Instant creationTime, Instant lastAccessTime,
                            Instant lastModifiedTime, Object fileKey) {

        static FileInfo of(final Path path) {
            try {
                final BasicFileAttributes attributes =
                        Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                final FileType type =
                        FileType.VALUES.findAny(ft -> ft.filter.test(attributes)).orElse(FileType.MISSING);
                return new FileInfo(
                        path, type, attributes.size(),
                        attributes.creationTime().toInstant(),
                        attributes.lastAccessTime().toInstant(),
                        attributes.lastModifiedTime().toInstant(),
                        attributes.fileKey());
            } catch (IOException e) {
                return new FileInfo(path, FileType.MISSING, 0, null, null, null, null);
            }
        }
    }
}
