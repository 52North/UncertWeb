package org.uncertweb.viss.mongo.resource;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.visualizer.Visualization;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Polymorphic;
import com.google.code.morphia.annotations.PostLoad;
import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.Property;
import com.google.code.morphia.annotations.Transient;

@Polymorphic
@Entity("resources")
public abstract class AbstractMongoResource implements Resource {

	public static final String TIME_PROPERTY = "last_usage";
	public static final String CHECKSUM_PROPERTY = "checksum";

	@Id
	private UUID uuid;
	private MediaType mediaType;
	@Indexed
	@Property(TIME_PROPERTY)
	private DateTime lastUsage;
	private File file;
	@Transient
	private Object content;
	@Indexed
	@Property(CHECKSUM_PROPERTY)
	private long checksum;

	@Embedded
	private Set<Visualization> visualizations = Utils.set();;

	public AbstractMongoResource(MediaType mt) {
		this.mediaType = mt;
	}

	@Override
	public boolean isLoaded() {
		return this.content != null;
	}

	@Override
	public UUID getUUID() {
		return this.uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public MediaType getMediaType() {
		return this.mediaType;
	}

	@Override
	public Object getResource() {
		return this.content;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public DateTime getLastUsage() {
		return lastUsage;
	}

	public void setLastUsage(DateTime lastUsage) {
		this.lastUsage = lastUsage;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	@Override
	public void suspend() {
		this.content = null;
	}
	
	@Override
	public void addVisualization(Visualization v) {
		this.visualizations.add(v);
	}

	public void removeVisualization(Visualization v) {
		this.visualizations.remove(v);
	}

	public Set<Visualization> getVisualizations() {
		return this.visualizations;
	}

	public long getChecksum() {
		return checksum;
	}

	public void setChecksum(long checksum) {
		this.checksum = checksum;
	}
	
	@PostLoad
	@PrePersist
	public void setLastUsageTime() {
		setLastUsage(new DateTime());
	}

}
