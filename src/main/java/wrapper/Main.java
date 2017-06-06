package wrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

	public static void main(final String[] args) {
		// Get the current working directory (assumed to be set properly)
		// [bwapi-data/AI]
		final Path working = Paths.get(System.getProperty("user.dir"));
		final Path writedir = working.getParent().resolve("write");
		// Make sure SWI is extracted to a specific directory when running
		// [bwapi-data/write/swi]
		SwiInstaller.overrideDirectory(writedir.resolve("swi").toString());
		// Get the agent code ZIP resource and extract it to its own directory
		// [bwapi-data/write/%name%]
		Path agentdir;
		try {
			final String name = "Bot";
			agentdir = writedir.resolve(name);
			unzip(name + ".zip", agentdir);
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}
		// Copy the connector.jar resource to its expected place
		// [bwapi-data/write]
		final Path env = writedir.resolve("connector.jar");
		try {
			final InputStream source = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(env.getFileName().toString());
			Files.copy(source, env, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}

		// Check if we have 1 mas2g and if it is error-free;
		// if it is, then modify its init parameters, and run it :)
		final SortedSet<File> mas2g = Run.getMASFiles(agentdir.toFile(), true);
		if (mas2g.size() == 1) {
			final MASProgram mas = parse(mas2g.iterator().next(), env.toFile());
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
			System.err.println("Found " + mas2g.size() + " mas2g files in " + agentdir);
		}
	}

	private static File unzip(final String zipfilename, final Path path) throws IOException {
		final File base = path.toFile();
		if (base.exists()) {
			deleteFolder(base);
		}

		System.out.println("Unzipping " + zipfilename + " to " + base);
		base.mkdirs();

		final InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(zipfilename);
		final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {
			final File file = new File(base, entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				final File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
				byte[] buffer = new byte[4096];
				int count = -1;
				while ((count = zis.read(buffer)) > 0) {
					out.write(buffer, 0, count);
				}
				out.close();
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
			mas2g.addInitParameter("game_speed", 50);
			mas2g.addInitParameter("own_race", "random");
			mas2g.addInitParameter("starcraft_location", "C:\\Starcraft");
			return mas2g;
		} else {
			System.err.println(mas.getName() + " invalid: " + validator.getSyntaxErrors());
			return null;
		}
	}
}
