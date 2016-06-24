package de.gwdg.europeanaqa.client.rest;

import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonWriterSettings;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class DocumentTransformer {

	Logger logger = Logger.getLogger(DocumentTransformer.class.getCanonicalName());

	DocumentCodec codec;
	MongoDatabase mongoDb;

	private final static Map<String, String> entities = new LinkedHashMap<String, String>() {
		{
			put("agents", "edm:Agent");
			put("concepts", "skos:Concept");
			put("timespans", "edm:TimeSpan");
			put("places", "edm:Place");
			put("licenses", "cc:License");

			put("aggregations", "ore:Aggregation");
			put("providedCHOs", "edm:ProvidedCHO");
			put("proxies", "ore:Proxy");
			put("europeanaAggregation", "edm:EuropeanaAggregation");
		}
	};

	private final static Map<String, String> subEntities = new LinkedHashMap<String, String>() {
		{
			put("webResources", "edm:WebResource");
		}
	};

	private final static List<String> languageFields = Arrays.asList(
			  "prefLabel", "altLabel", "note",

			  "edmDataProvider", "edmProvider", "edmRights", "edmLanguage",
			  "edmCountry", "year", "begin", "edmCurrentLocation", "end",
			  "edmHasMet", "edmHasType", "hasView", "edmIncorporates",
			  "edmIsDerivativeOf", "edmIsNextInSequence", "edmIsRelatedTo",
			  "edmIsRepresentationOf", "edmIsSimilarTo", "edmIsSuccessorOf",
			  "edmIsShownBy", "edmIsShownAt", "edmLanguage", "edmLandingPage",
			  "edmObject", "edmPreview", "edmProvider", "edmRealizes",
			  "edmRights", "edmType", "edmUgc", "edmUnstored", "edmPreviewNoDistribute",

			  "dcContributor", "dcCoverage", "dcSubject", "dcCreator", "dcDate",
			  "dcDescription", "dcFormat", "dcIdentifier", "dcLanguage",
			  "dcPublisher", "dcRelation", "dcRights", "dcSource", "dcSubject",
			  "dcTitle", "dcType", "dctermsTOC",

			  "dctermsAlternative", "dctermsCreated", "dctermsExtent", "dctermsHasPart",
			  "dctermsIsFormatOf", "dctermsIsPartOf", "dctermsIsReferencedBy",
			  "dctermsIssued", "dctermsIsVersionOf", "dctermsMedium",
			  "dctermsProvenance", "dctermsReferences", "dctermsSpatial", "isPartOf", "dctermsTemporal",

			  "rdaGr2BiographicalInformation", "rdaGr2DateOfBirth", "rdaGr2DateOfDeath",
			  "rdaGr2DateOfEstablishment", "rdaGr2DateOfTermination", "rdaGr2Gender",
			  "rdaGr2ProfessionOrOccupation", "rdaGr2PlaceOfBirth", "rdaGr2PlaceOfDeath",

			  "foafName"
	);

	private final static Map<String, String> fieldDictionary = new HashMap<String, String>() {
		{
			put("about", "@about");

			// dc
			put("dcContributor", "dc:contributor");
			put("dcCoverage", "dc:coverage");
			put("dcCreator", "dc:creator");
			put("dcDate", "dc:date");
			put("dcDescription", "dc:description");
			put("dcFormat", "dc:format");
			put("dcIdentifier", "dc:identifier");
			put("dcLanguage", "dc:language");
			put("dcPublisher", "dc:publisher");
			put("dcRelation", "dc:relation");
			put("dcRights", "dc:rights");
			put("dcSource", "dc:source");
			put("dcSubject", "dc:subject");
			put("dcTitle", "dc:title");
			put("dcType", "dc:type");

			// dcterms
			put("dctermsAlternative", "dcterms:alternative");
			put("dctermsConformsTo", "dcterms:conformsTo");
			put("dctermsCreated", "dcterms:created");
			put("dctermsExtent", "dcterms:extent");
			put("dctermsHasFormat", "dcterms:hasFormat");
			put("dctermsHasPart", "dcterms:hasPart");
			put("dctermsHasVersion", "dcterms:hasVersion");
			put("dctermsIsFormatOf", "dcterms:isFormatOf");
			put("dctermsIsPartOf", "dcterms:isPartOf");
			put("isPartOf", "dcterms:isPartOf");
			put("dctermsIsReferencedBy", "dcterms:isReferencedBy");
			put("dctermsIsReplacedBy", "dcterms:isReplacedBy");
			put("dctermsIsRequiredBy", "dcterms:isRequiredBy");
			put("dctermsIssued", "dcterms:issued");
			put("dctermsIsVersionOf", "dcterms:isVersionOf");
			put("dctermsMedium", "dcterms:medium");
			put("dctermsProvenance", "dcterms:provenance");
			put("dctermsReferences", "dcterms:references");
			put("dctermsReplaces", "dcterms:replaces");
			put("dctermsRequires", "dcterms:requires");
			put("dctermsSpatial", "dcterms:spatial");
			put("dctermsTOC", "dcterms:tableOfContents");
			put("dctermsTemporal", "dcterms:temporal");

			// skos
			put("altLabel", "skos:altLabel");
			put("prefLabel", "skos:prefLabel");
			put("related", "skos:related");
			put("related", "skos:related");
			put("note", "skos:note");
			put("broader", "skos:broader");
			put("narrower", "skos:narrower");
			put("broadMatch", "skos:broadMatch");
			put("narrowMatch", "skos:narrowMatch");
			put("exactMatch", "skos:exactMatch");
			put("relatedMatch", "skos:relatedMatch");
			put("closeMatch", "skos:closeMatch");
			put("notation", "skos:notation");
			put("inScheme", "skos:inScheme");

			// edm
			put("aggregatedCHO", "edm:aggregatedCHO");
			put("begin", "edm:begin");
			put("edmCountry", "edm:country");
			put("edmCurrentLocation", "edm:currentLocation");
			put("edmDataProvider", "edm:dataProvider");
			put("end", "edm:end");
			put("europeanaProxy", "edm:europeanaProxy");
			put("edmHasMet", "edm:hasMet");
			put("edmHasType", "edm:hasType");
			put("hasView", "edm:hasView");
			put("edmHasView", "edm:hasView");
			put("edmIncorporates", "edm:incorporates");
			put("edmIsDerivativeOf", "edm:isDerivativeOf");
			put("edmIsNextInSequence", "edm:isNextInSequence");
			put("edmIsRelatedTo", "edm:isRelatedTo");
			put("edmIsRepresentationOf", "edm:isRepresentationOf");
			put("edmIsSimilarTo", "edm:isSimilarTo");
			put("edmIsSuccessorOf", "edm:isSuccessorOf");
			put("edmIsShownBy", "edm:isShownBy");
			put("edmIsShownAt", "edm:isShownAt");
			put("edmLanguage", "edm:language");
			put("edmLandingPage", "edm:landingPage");
			put("edmObject", "edm:object");
			put("edmPreview", "edm:preview");
			put("edmProvider", "edm:provider");
			put("edmRealizes", "edm:realizes");
			put("edmRights", "edm:rights");
			put("edmType", "edm:type");
			put("edmUgc", "edm:ugc");
			put("edmUnstored", "edm:unstored");
			put("year", "edm:year");
			// not in http://labs.europeana.eu/api/data-fields
			put("edmPreviewNoDistribute", "edm:previewNoDistribute");

			// ore
			put("proxyIn", "ore:proxyIn");
			put("proxyFor", "ore:proxyFor");
			put("aggregates", "ore:aggregates");

			// wgs84 or wgs84_pos?
			put("longitude", "wgs84:long");
			put("latitude", "wgs84:lat");
			put("altitude", "wgs84:alt");

			// owl
			put("owlSameAs", "owl:sameAs");

			// rdaGr2
			put("rdaGr2BiographicalInformation", "rdaGr2:biographicalInformation");
			put("rdaGr2DateOfBirth", "rdaGr2:dateOfBirth");
			put("rdaGr2DateOfDeath", "rdaGr2:dateOfDeath");
			put("rdaGr2DateOfEstablishment", "rdaGr2:dateOfEstablishment");
			put("rdaGr2DateOfTermination", "rdaGr2:dateOfTermination");
			put("rdaGr2Gender", "rdaGr2:gender");
			put("rdaGr2ProfessionOrOccupation", "rdaGr2:professionOrOccupation");
			// not in http://labs.europeana.eu/api/data-fields
			// in /11004/E66D8929E1ABD5BDD48E64E86D12EAEB7760AA60
			put("rdaGr2PlaceOfBirth", "rdaGr2:placeOfBirth");
			put("rdaGr2PlaceOfDeath", "rdaGr2:placeOfDeath");

			// foaf
			put("foafName", "foaf:name");

			// odlr
			put("odrlInheritFrom", "odrl:inheritFrom");

			// cc
			put("ccDeprecatedOn", "cc:deprecatedOn");
		}
	};

	public DocumentTransformer(MongoDatabase mongoDb) {
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry());
		codec = new DocumentCodec(codecRegistry, new BsonTypeClassMap());

		this.mongoDb = mongoDb;
	}

	public void transform(Document record) {
		record.remove("_id");
		record.remove("className");
		record.put("identifier", record.get("about"));
		record.remove("about");
		record.put("sets", record.get("europeanaCollectionName"));
		for (String entity : entities.keySet()) {
			if (record.containsKey(entity)) {
				Object value = record.get(entity);
				if (value instanceof List) {
					List<DBRef> refs = (List<DBRef>) record.get(entity);
					if (refs != null && refs.size() > 0) {
						List<Document> transformedValues = new ArrayList<>();
						for (DBRef ref : refs) {
							Document doc = resolveReference(ref);
							transformedValues.add(doc);
						}
						record.remove(entity);
						record.put(entities.get(entity), transformedValues);
					} else {
						System.err.println("EMPTY: " + entity + " " + refs);
						record.remove(entity);
					}
				} else if (value instanceof DBRef) {
					record.remove(entity);
					record.put(entities.get(entity), resolveReference((DBRef) value));
				} else {
					System.err.println("UNKNOWN: " + entity + " " + value.getClass().getCanonicalName());
				}
			}
		}

	}

	private Document resolveReference(DBRef ref) {
		String collection = ref.getCollectionName();
		Document doc = mongoDb.getCollection(collection).find(Filters.eq("_id", ref.getId())).first();
		if (doc != null) {
			doc.remove("_id");
			doc.remove("className");
			transformLanguageStructure(doc);
			replaceKeys(doc);
			for (String key : subEntities.keySet()) {
				if (doc.containsKey(key)) {
					List<Document> subDocs = new ArrayList<Document>();
					List<DBRef> subRefs = (List<DBRef>) doc.get(key);
					for (DBRef subRef : subRefs) {
						subDocs.add(resolveReference(subRef));
					}
					doc.remove(key);
					doc.put(subEntities.get(key), subDocs);
				}
			}
		}
		return doc;
	}

	private void replaceKeys(Document doc) {
		for (Map.Entry<String, String> field : fieldDictionary.entrySet()) {
			replaceKey(doc, field.getKey(), field.getValue());
		}
	}

	private void replaceKey(Document doc, String from, String to) {
		if (doc.containsKey(from)) {
			doc.put(to, doc.get(from));
			doc.remove(from);
		}
	}

	private void transformLanguageStructure(Document doc) {
		for (String field : fieldDictionary.keySet()) {
			if (doc.containsKey(field)
					&& doc.get(field) instanceof Document)
			{
				replaceLanguage(doc, field);
			}
		}
	}

	private void replaceLanguage(Document doc, String key) {
		Document field = (Document) doc.get(key);
		List<Object> instances = new ArrayList<>();
		for (String lang : field.keySet()) {
			List<String> values = (List<String>) field.get(lang);
			if (values != null && values.size() > 0) {
				for (String value : values) {
					if (!lang.equals("def")) {
						Document instance = new Document();
						instance.append("@lang", lang);
						instance.append("#value", value);
						instances.add(instance);
					} else {
						instances.add(value);
					}
				}
			}
		}
		doc.put(key, instances);
	}

}
