package fr.pickaria.emerald;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class Loader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        classpathBuilder.addLibrary(getKotlinResolver());
        classpathBuilder.addLibrary(getPickariaResolver());
    }

    private MavenLibraryResolver getKotlinResolver() {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("maven-central", "default", "https://repo.maven.apache.org/maven2/").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.0.0"), null));
        return resolver;
    }

    private MavenLibraryResolver getPickariaResolver() {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("pickaria", "default", "https://maven.pickaria.fr/snapshots").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("fr.pickaria:bookshelf-lib:1.1-SNAPSHOT"), null));
        return resolver;
    }
}
