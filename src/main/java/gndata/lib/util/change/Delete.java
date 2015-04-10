package gndata.lib.util.change;

import java.util.*;
import java.util.stream.Collectors;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Class that knows how to delete Metadata entities.
 */
public class Delete implements Change {

    private Resource res;
    private boolean isRecursive = true;
    private boolean applied = false;

    private Model changes = ModelFactory.createDefaultModel();

    public Delete(Resource res, boolean isRecursive) {
        this.res = res;
        this.isRecursive = isRecursive;
    }

    private void deleteSingle(Resource res, Model from) {
        if (from.containsResource(res)) {
            Model tmp = ModelFactory.createDefaultModel();
            tmp.add(from);

            Set<Resource> related = from.listObjectsOfProperty(res, null).toList()
                    .stream().filter(r -> r.isResource())
                    .map(RDFNode::asResource).collect(Collectors.toSet());

            // delete all properties
            from.removeAll(res, null, null);

            // record changes
            changes.add(from.difference(tmp));

            // delete all res-to-object entities that have no other references
            if (isRecursive) {
                related.forEach(r -> {
                    if (from.listSubjectsWithProperty(null, r.getURI())
                            .toList().isEmpty()) {
                        deleteSingle(r, from);
                    }
                });
            }
        }

    }

    public void applyTo(Model m) {
        deleteSingle(res, m);
        applied = true;
    }

    public void undoFrom(Model m) {
        // TODO add logic
    }

    public boolean applied() {
        return applied;
    }
}
