package de.flapdoodle.embed.processg.parts;

import de.flapdoodle.embed.process.config.store.ProxyFactory;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.processg.config.store.DownloadConfig;
import de.flapdoodle.embed.processg.config.store.Package;
import de.flapdoodle.embed.processg.net.UrlStreams;
import de.flapdoodle.embed.processg.store.ArchiveStore;
import de.flapdoodle.embed.processg.store.Downloader;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.types.ThrowingSupplier;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Value.Immutable
public abstract class DownloadPackage implements Transition<Archive> {

	protected abstract String name();

	protected abstract ArchiveStore archiveStore();

	@Value.Default
	protected UrlStreams.DownloadCopyListener downloadCopyListener() {
		return (bytesCopied, contentLength) -> {
		};
	}

	@Value.Default
	protected DownloadConfig downloadConfig() {
		return DownloadConfig.defaults();
	}

	@Value.Default
	protected ThrowingSupplier<Path, IOException> tempDir() {
		return () -> Files.createTempDirectory(name());
	}

	@Value.Default
	protected StateID<Distribution> distribution() {
		return StateID.of(Distribution.class);
	}

	@Value.Default
	protected StateID<Package> distPackage() {
		return StateID.of(Package.class);
	}

	@Override
	@Value.Default
	public StateID<Archive> destination() {
		return StateID.of(Archive.class);
	}

	@Override
	@Value.Auxiliary
	public final Set<StateID<?>> sources() {
		return StateID.setOf(distribution(), distPackage());
	}

	@Override
	@Value.Auxiliary
	public State<Archive> result(StateLookup lookup) {
		Distribution dist = lookup.of(distribution());
		Package distPackage = lookup.of(distPackage());

		Optional<Path> archive = archiveStore().archiveFor(name(), dist, distPackage.archiveType());
		if (archive.isPresent()) {
			return State.of(archive.map(Archive::of).get());
		} else {
			Path downloadedArchive = Try.supplier(tempDir())
				.mapCheckedException(cause -> new IllegalStateException("could not create archive path", cause))
				.get()
				.resolve(UUID.randomUUID().toString());

			Try.runable(() -> {
					URL downloadUrl = new URL(distPackage.url());
					URLConnection connection = UrlStreams.urlConnectionOf(downloadUrl, downloadConfig().getUserAgent(), downloadConfig().getTimeoutConfig(),
						downloadConfig().proxyFactory().map(ProxyFactory::createProxy));
					UrlStreams.downloadTo(connection, downloadedArchive, downloadCopyListener());
				}).mapCheckedException(cause -> new IllegalStateException("could not download "+distPackage.url(), cause))
				.run();

			Path storedArchive = Try.supplier(() -> archiveStore().store(name(), dist, distPackage.archiveType(), downloadedArchive))
				.mapCheckedException(cause -> new IllegalArgumentException("could not store downloaded artifact", cause))
				.get();
			
			return State.of(Archive.of(storedArchive), it -> {
				Try.run(() -> Files.delete(it.value()));
			});
		}
	}

	public static ImmutableDownloadPackage.Builder builder() {
		return ImmutableDownloadPackage.builder();
	}

}
