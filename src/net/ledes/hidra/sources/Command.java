package net.ledes.hidra.sources;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.jws.WebMethod;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class Command {

	private Hidra hidra;

	public Hidra getHidra() {
		return hidra;
	}

	public void setHidra(Hidra hidra) {
		this.hidra = hidra;
	}

	public void inicializar(File directory, Git Auxiliary) {

		hidra = new Hidra(Auxiliary);
		hidra.setLocalPath(directory.getAbsolutePath());

	}

	public boolean adicionar(String fileName) {
		File file;
		String extension[];

		if (hidra == null) {
			System.err.println("Repository uninitialized");
		} else {
			file = new File(hidra.getLocalPath() + "/" + fileName);
			if (!file.exists()) {
				System.err.println("file does not exist");
				return false;
			} else {
				extension = fileName.split("\\.");
				if (!extension[1].equals("txt")) {
					System.err.println("Incorrect file type: " + extension[1]);
					return false;
				} else {
					try {
						hidra.getGit().add().addFilepattern(fileName).call();
						hidra.setAdded(hidra.getGit().status().call()
								.getAdded());
					} catch (GitAPIException e) {
						System.out.println(e.getMessage());
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean remove(String filename) {
		File file;

		if (hidra == null) {
			System.err.println("Repository uninitialized");
		} else {

			file = new File(hidra.getLocalPath() + "/" + filename);

			if (!file.exists()) {
				System.err.println("file does not exist");
			} else {
				try {
					hidra.getGit().rm().addFilepattern(filename).call();
					hidra.setRemoved(this.hidra.getGit().status().call()
							.getUntracked());
				} catch (GitAPIException e) {
					System.out.println(e.getMessage());
				}

				if (this.hidra.getRemoved() != null) {
					System.out.println("Successfully excluded file");
				}
			}
			return true;
		}
		return false;
	}

	public boolean commit(String message) {

		if (hidra == null) {
			System.err.println("Repository uninitialized");
		} else {
			try {
				RevCommit commit = hidra.getGit().commit().setMessage(message)
						.call();
				System.out.println(commit.getId().getName());
			} catch (GitAPIException e) {
				System.out.println(e.getMessage());
			}
			return true;
		}
		return false;
	}

	public boolean cloneW(String remotePath, File directory)
			throws IOException, InvalidRemoteException, TransportException,
			GitAPIException {

		if (directory.exists() && directory.listFiles().length != 0) {
			System.out.println("Repository not empty , Canceled Operation");
			return false;
		} else {
			Git result = Git.cloneRepository().setURI(remotePath)
					.setDirectory(directory).call();

			try {
				System.out.println("Repository successfully cloned "
						+ result.getRepository().getDirectory());
			} finally {
				result.close();
			}
			return true;
		}
	}

	public String status() {
		if (hidra == null) {
			System.err.println("Repository uninitialized");
		} else {
			try {
				hidra.setStatus(hidra.getGit().status().call());
				String showStatus = "Added: "
						+ this.hidra.getStatus().getAdded() + "\nChanged"
						+ this.hidra.getStatus().getChanged()
						+ "\nConflicting: "
						+ this.hidra.getStatus().getConflicting()
						+ "\nConflictingStageState: "
						+ this.hidra.getStatus().getConflictingStageState()
						+ "\nIgnoredNotInIndex: "
						+ this.hidra.getStatus().getIgnoredNotInIndex()
						+ "\nMissing: " + this.hidra.getStatus().getMissing()
						+ "\nModified: " + this.hidra.getStatus().getModified()
						+ "\nRemoved: " + this.hidra.getStatus().getRemoved()
						+ "\nUntracked: "
						+ this.hidra.getStatus().getUntracked()
						+ "\nUntrackedFolders: "
						+ this.hidra.getStatus().getUntrackedFolders()
						+ "\nUncommitted Changes"
						+ this.hidra.getStatus().getUncommittedChanges();

				return showStatus;
			} catch (NoWorkTreeException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getLogs() {
		String logs = null;
		if (hidra == null) {
			System.err.println("Repository uninitialized");
		} else {

			// Repository repository1 = git1.getRepository();
			//ObjectId head = repository1.resolve("HEAD");
			Iterable<RevCommit> log = null;
			try {
				log = hidra.getGit().log().call();
			} catch (NoHeadException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}

			@SuppressWarnings("rawtypes")
			Iterator itr = log.iterator();

			while (itr.hasNext()) {
				// Object element = itr.next();
				RevCommit rev = (RevCommit) itr.next();
				// System.out.println(element);
				logs = "Author: " + rev.getAuthorIdent().getName()
						+ "\nMessage: " + rev.getFullMessage();
				/*
				 * System.out.println("Author: " +
				 * rev.getAuthorIdent().getName()); //$NON-NLS-1$
				 * System.out.println("Message: " + rev.getFullMessage());
				 * //$NON-NLS-1$ System.out.println();
				 */
				return logs;
			}

		}
		return logs;
	}

	@WebMethod
	public String showBranch() {
		String branches = null;
		if (hidra == null) {
			System.err.println("Repository uninitialized");
		} else {
			List<org.eclipse.jgit.lib.Ref> call = null;
			try {
				call = new Git(hidra.getGit().getRepository()).branchList()
						.call();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// repensar mostrar ou não o id do branch
			for (org.eclipse.jgit.lib.Ref ref : call) {
				branches = "Branch: " + ref.getName();
			}
		}
		hidra.getGit().getRepository().close();
		return branches;
	}

	public String createBranch(String nameBranch) {
		String branch = null;
		if (hidra == null) {
			System.err.println("Repositorio nao inicializado");
		} else {
			try {
				hidra.getGit().branchCreate().setName(nameBranch).call();

			} catch (RefAlreadyExistsException e1) {

				e1.printStackTrace();
			} catch (RefNotFoundException e1) {

				e1.printStackTrace();
			} catch (InvalidRefNameException e1) {

				e1.printStackTrace();
			} catch (GitAPIException e1) {

				e1.printStackTrace();
			}

			List<org.eclipse.jgit.lib.Ref> call = null;
			try {
				call = new Git(hidra.getGit().getRepository()).branchList()
						.call();
			} catch (GitAPIException e) {

				e.printStackTrace();
			}

			for (org.eclipse.jgit.lib.Ref ref : call) {
				branch = "Branch Created: " + " " + ref.getName();

			}
			hidra.getGit().getRepository().close();

			return branch;
		}
		return branch;
	}

	public void pull() {

		PullResult pullResult = null;
		try {
			pullResult = hidra.getGit().pull().call();
		} catch (NoHeadException e) {

			e.printStackTrace();
		} catch (TransportException e) {

			e.printStackTrace();
		} catch (GitAPIException e) {

			e.printStackTrace();
		}
		System.out.println(pullResult);

		MergeResult mergeResult = pullResult.getMergeResult();
		if (mergeResult != null) {

		}
		RebaseResult rebaseResult = pullResult.getRebaseResult();
		if (rebaseResult != null) {

		}

	}

	public void push() {

		// Colocar usuario e senha nos "", ""

		CredentialsProvider cp = new UsernamePasswordCredentialsProvider("", "");
		PushCommand pc = hidra.getGit().push();
		pc.setCredentialsProvider(cp).setForce(true).setPushAll();
		Iterator<PushResult> it = null;

		try {
			it = pc.call().iterator();
		} catch (TransportException e) {

			e.printStackTrace();
		} catch (GitAPIException e) {

			e.printStackTrace();
		}
		if (it.hasNext()) {
			System.out.println(it.next().toString());
		}
	}

	public void getDiff(String branch1, String branch2) {

		Git gitDiff = hidra.getGit();
		Repository repo = gitDiff.getRepository();
		AbstractTreeIterator oldTreeParser = prepareTreeParser(repo,
				"refs/heads/" + branch1);
		AbstractTreeIterator newTreeParser = prepareTreeParser(repo,
				"refs/heads/" + branch2);

		List<DiffEntry> diff = null;
		try {
			diff = new Git(repo).diff().setOldTree(oldTreeParser)
					.setNewTree(newTreeParser).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		for (DiffEntry entry : diff) {
			System.out.println("Entry: " + entry);
		}

		repo.close();
	}

	public AbstractTreeIterator prepareTreeParser(Repository repository,
			String ref) {
		
		Ref head = null;
		
		try {
			head = repository.getRef(ref);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = null;
		
		try {
			commit = walk.parseCommit(head.getObjectId());
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RevTree tree = null;
		
		try {
			tree = walk.parseTree(commit.getTree().getId());
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		ObjectReader oldReader = repository.newObjectReader();
		
		try {
			try {
				oldTreeParser.reset(oldReader, tree.getId());
			} catch (IncorrectObjectTypeException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			oldReader.release();
		}
		return oldTreeParser;
	}
}
