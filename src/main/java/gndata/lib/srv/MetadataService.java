// Copyright (c) 2014, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package gndata.lib.srv;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

import static java.util.Spliterator.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.vocabulary.*;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.riot.RDFDataMgr;

/**
 * Class implementing main functions working with project metadata
 */
public class MetadataService {

    public static final String stdPrefix = StrUtils.strjoinNL(
            "PREFIX rdf: <" + RDF.getURI() + ">",
            "PREFIX rdfs: <" + RDFS.getURI() + ">",
            "PREFIX owl: <" + OWL.getURI() + ">"
    );

    private OntModel schema;    // union model for all imported ontology files
    private Model annotations;  // model for data annotations

    public MetadataService(OntModel schema, Model annotations) {
        this.schema = schema;
        this.annotations = annotations;
    }

    /**
     * Provides a string with available PREFIX'es. May be used to build queries
     * against current service.
     *
     * @return  String with prefixes
     */
    public String getPrefixHeader() {
        return StrUtils.strjoinNL(getAnnotations()
                    .getNsPrefixMap()
                    .entrySet()
                    .stream()
                    .map(a -> "PREFIX " + a.getKey() + ": " + "<" + a.getValue() + ">")
                    .collect(Collectors.toList()));
    }

    /**
     * Returns a Ontology RDF Model instance to access default and
     * custom ontology terms.
     *
     * @return Ontology Model
     */
    public OntModel getSchema() {
        return schema;
    }

    /**
     * Returns an RDF Model instance to access annotations as stored RDF triples.
     *
     * @return RDF Model
     */
    public Model getAnnotations() {
        return annotations;
    }

    /**
     * Filters annotation literals by a given string.
     *
     * @return RDF Model with Subjects with matched literals and their RDF:types.
     */
    public Model getAnnotations(String literalFilter) {
        if (literalFilter.length() > 0) {
            String qs = StrUtils.strjoinNL(
                    "CONSTRUCT { ",
                    "?s rdf:type ?t .",
                    "?s ?p ?o",
                    "}",
                    "WHERE { ",
                    "?s rdf:type ?t .",
                    "?s ?p ?o . ",
                    "FILTER (",
                    "(STR(?p) != rdf:type) && ",
                    "isLiteral(?o) && ",
                    "regex(?o, '" + literalFilter + "', 'i')",
                    ")}"
            );

            return CONSTRUCT(stdPrefix + "\n" + qs);
        } else {
            return getAnnotations();
        }
    }

    /**
     * Fulltext search on literal values inside the metadata model.
     *
     * @param search The search parameter.
     *
     * @return A stream of resources with a property having a part of the
     *         search string as literal value.
     */
    public Stream<Resource> streamSearchResults(String search) {
        String qs = StrUtils.strjoin("",
                "SELECT DISTINCT ?s ",
                "WHERE {",
                "  ?s ?p ?o ",
                "  FILTER regex(?o, '", search, "', 'i')}"
        );

        Query query = QueryFactory.create(qs);
        ResultSet rs = QueryExecutionFactory.create(query, getAnnotations()).execSelect();

        String var = rs.getResultVars().get(0);
        Stream<QuerySolution> stream =StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(rs, NONNULL | IMMUTABLE | DISTINCT), false);

        return stream.map(sol -> sol.getResource(var));
    }

    /**
     * Fulltext search on literal values inside the metadata model.
     *
     * @param search The search parameter.
     *
     * @return A stream of resources with a property having a part of the
     *         search string as literal value.
     */
    public List<Resource> listSearchResults(String search) {
        return streamSearchResults(search).collect(Collectors.toList());
    }

    /**
     * Creates a new model with inferred relations based on loaded ontology,
     * annotations and reasoner.
     *
     * @return Model with inferred relations
     */
    public InfModel getAnnotationsWithInference() {
        return ModelFactory.createInfModel(getReasoner(), annotations);
    }

    /**
     * Creates a new reasoner based on actual schema and annotations.
     *
     * @return Reasoner
     */
    public Reasoner getReasoner() {
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        return reasoner.bindSchema(schema);
    }

    /**
     * Returns available types (resources) in the current annotations.
     *
     * @return List<Resource>
     */
    public List<Resource> getAvailableTypes() {
        return getAnnotations().listObjectsOfProperty(RDF.type).toList()
                .stream()
                .map(RDFNode::asResource)
                .filter(r -> !r.getNameSpace().equals(OWL.getURI()))
                .collect(Collectors.toList());
    }

    public ResultSet SELECT(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, getAnnotations());

        ResultSet resultModel = ResultSetFactory.copyResults(qexec.execSelect());
        qexec.close();

        return resultModel;
    }

    public Model CONSTRUCT(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, getAnnotations());

        Model resultModel = qexec.execConstruct();
        qexec.close();

        return resultModel;
    }

    public void importMetadata(String path) {
        Model newData =  RDFDataMgr.loadModel(path);
        annotations.add(newData);

        Model foo = ModelFactory.createDefaultModel();
    }

    /*
    New API methods
     */

    public void createEntity(Model m) {
        // model should have an RDF.type

        // validate model against OWL

        // add to annotations

        // save changes (optional)
    }

    public void deleteEntity(Resource res) {
        // delete all literals

        // delete all subject-to-res relations

        // delete all res-to-object entities that have no other references
    }

    public void updateDataProperty(Resource res, Property p, String value) {
        // convert and validate from string using OWL
    }


    /**
     * Creates a new Metadata Service using a given path. Combines existing
     * project RDF schemas (ontology files) and metadata storage (annotations)
     * into a common Model. Creates default schemas if some do not exist.
     *
     * @return MetadataService
     */
    public static MetadataService create(String projectPath) throws IOException {
        if (projectPath == null) {
            throw new IOException("Cannot create metadata service at non-existing path");
        }

        MetadataFilesManager metaFiles = new MetadataFilesManager(projectPath);

        OntModel schema = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);

        // TODO figure out if using OntDocumentManager to read files makes sense
        for (Path p : metaFiles.schemaPaths()) {
            schema.addSubModel(RDFDataMgr.loadModel(p.toString()));
        }

        Model data = RDFDataMgr.loadModel(metaFiles.annotationsPath().toString());

        return new MetadataService(schema, data);
    }
}
