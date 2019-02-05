package com.aem.sumanta.dbpedia;

import com.aem.sumanta.dbpedia.ontology.UriRef;

public class NameOccurrence {

	public final String name;
	public final UriRef type;
	public final String context;

	public NameOccurrence(String name, UriRef type, String context) {
		this.name = name;
		this.type = type;
		this.context = context;

	}

	public UriRef getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("[name='%s',  type='%s', context='%s']", name,
				type, context);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NameOccurrence) {
			NameOccurrence temp = (NameOccurrence) obj;
			if (this.name == temp.name && this.type == temp.type
					&& this.context == temp.context)
				return true;
		}
		return false;

	}

	@Override
	public int hashCode() {
		return (this.name.hashCode() + this.type.hashCode() + this.context
				.hashCode());
	}
}
