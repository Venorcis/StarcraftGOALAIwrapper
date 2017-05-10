package wrapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import goal.preferences.CorePreferences;
import goal.tools.Run;
import goal.tools.SingleRun;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;
import languageTools.program.mas.MASProgram;
import swiprolog.SwiInstaller;

public class Main {
	private final static String name = "";

	public static void main(final String[] args) {
		final Path working = Paths.get(System.getProperty("user.dir")); // bwapi-data/AI
		final Path writedir = working.getParent().resolve("write");
		SwiInstaller.overrideDirectory(writedir.toString());
		final Path agentdir = writedir.resolve(name);
		try {
			unzip(name, agentdir);
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}
		File env = null;
		try {
			env = new File(Thread.currentThread().getContextClassLoader().getResource("connector.jar").toURI());
		} catch (final URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		final SortedSet<File> mas2g = Run.getMASFiles(agentdir.toFile(), true);
		if (mas2g.size() == 1) {
			final MASProgram mas = parse(mas2g.iterator().next(), env);
			if (mas != null) {
				try {
					CorePreferences.setRemoveKilledAgent(true);
					final SingleRun run = new SingleRun(mas);
					run.run(true);
				} catch (final GOALRunFailedException e) {
					e.printStackTrace();
				} finally {
					System.exit(0);
				}
			}
		} else {
			System.err.println("Found " + mas2g.size() + " test2g files in " + agentdir);
		}
	}

	private static File unzip(final String zipfilename, final Path path) throws IOException {
		final File base = path.toFile();
		if (base.exists()) {
			deleteFolder(base);
		}

		System.out.println("unzipping " + zipfilename + " to " + base);
		base.mkdir();

		final InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(zipfilename);
		final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry = null;
		byte[] buffer = new byte[2048];
		while ((entry = zis.getNextEntry()) != null) {
			final File fileInDir = new File(base, entry.getName());
			if (entry.isDirectory()) {
				fileInDir.mkdir();
			} else if (!fileInDir.canRead()) {
				final FileOutputStream fOutput = new FileOutputStream(fileInDir);
				int count = 0;
				while ((count = zis.read(buffer)) > 0) {
					fOutput.write(buffer, 0, count);
				}
				fOutput.close();
			}
			zis.closeEntry();
		}
		zis.close();
		fis.close();

		return base;
	}

	private static void deleteFolder(final File folder) {
		final File[] files = folder.listFiles();
		if (files != null) {
			for (final File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	private static MASProgram parse(final File mas, final File env) {
		final MASValidator validator = new MASValidator(mas.getPath(), new FileRegistry());
		validator.validate();
		if (validator.getProgram() != null) {
			validator.process();
			final MASProgram mas2g = validator.getProgram();
			mas2g.setEnvironmentfile(env);
			mas2g.resetInitParameters();
			mas2g.addInitParameter("auto_menu", "OFF");
			mas2g.addInitParameter("debug", "false");
			mas2g.addInitParameter("invulnerable", "false");
			mas2g.addInitParameter("game_speed", "-1");
			return mas2g;
		} else {
			System.err.println(mas.getName() + " invalid: " + validator.getSyntaxErrors());
			return null;
		}
	}
}
