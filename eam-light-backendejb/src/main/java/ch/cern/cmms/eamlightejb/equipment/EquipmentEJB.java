package ch.cern.cmms.eamlightejb.equipment;

import ch.cern.eam.wshub.core.repositories.EquipmentChildrenRepository;
import ch.cern.eam.wshub.core.repositories.EquipmentTreeNodeRepository;
import ch.cern.eam.wshub.core.services.equipment.entities.EquipmentChildren;
import ch.cern.eam.wshub.core.services.equipment.entities.EquipmentTreeNode;
import ch.cern.cmms.eamlightejb.equipment.tools.GraphNode;
import ch.cern.cmms.eamlightejb.index.IndexEJB;
import ch.cern.cmms.eamlightejb.index.IndexGrids;
import ch.cern.cmms.eamlightejb.index.IndexResult;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.services.entities.Entity;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EquipmentEJB {

	@Autowired
	private InforClient inforClient;

	@Autowired
	private IndexEJB indexEJB;

	@Autowired
	private IndexGrids indexGrids;

	@Autowired(required = false)
	private EquipmentChildrenRepository equipmentChildrenRepository;

	@Autowired(required = false)
	private EquipmentTreeNodeRepository equipmentTreeNodeRepository;

	/**
	 * Default constructor.
	 */
	public EquipmentEJB() {

	}

	public List<EquipmentChildren> getEquipmentChildren(String equipment) {
		if (equipmentChildrenRepository != null) {
			return equipmentChildrenRepository.getEquipmentChildren(equipment);
		}
		return new ArrayList<>();
	}

	public List<Entity> getEquipmentSearchResults(String code, List<String> customEntityTypes, InforContext inforContext) throws InforException {
		return getEquipmentSearchResults(code, customEntityTypes, inforContext, null, 10);
	}

	public List<Entity> getEquipmentSearchResults(String code, List<String> customEntityTypes, InforContext inforContext, String entityClass, Integer rowCount) throws InforException {
		if (customEntityTypes == null) {
			customEntityTypes = Arrays.asList("A", "P", "S", "L");
		}

		List<IndexResult> indexResults;
		if (inforClient.getTools().isDatabaseConnectionConfigured()) {
			indexResults = indexEJB.getIndexResultsFaster(
					code,
					inforContext.getCredentials().getUsername(),
					customEntityTypes,
					entityClass
			);

		} else {
			indexResults = indexGrids.search(inforContext, code, customEntityTypes, entityClass, rowCount);
		}
		return indexResults.stream().map(r -> new Entity(r.getCode(), r.getDescription(), r.getOrganization())).collect(Collectors.toList());
	}

	public List<GraphNode> getEquipmentStructureTree(String equipment) {
		if (!inforClient.getTools().isDatabaseConnectionConfigured() || equipmentTreeNodeRepository == null) {
			return new LinkedList<>();
		}

		// Fetch tree as list
		List<EquipmentTreeNode> result = equipmentTreeNodeRepository.getTree(equipment);

		if (result == null || result.isEmpty()) {
			return new LinkedList<>();
		}

		// Remove root node since its parent is not included
		EquipmentTreeNode rootTreeNode = result.remove(0);
		GraphNode rootNode = new GraphNode(rootTreeNode.getId(), rootTreeNode.getName(), rootTreeNode.getType());

		// Keep cache of nodes so that, when adding a child, it adds throughout the tree (same equipment might have
		// more than one parent
		Map<String, GraphNode> graphNodeMap = new HashMap<>();
		graphNodeMap.put(rootNode.getId(), rootNode);

		// Add the relationships between nodes, create as necessary
		for(EquipmentTreeNode node : result) {
			GraphNode graphNode = graphNodeMap.computeIfAbsent(
					node.getId(),
					k -> new GraphNode(node.getId(), node.getName(), node.getType())
			);

			//Nodes may have parents that are not yet on the tree. Those are ignored.
			graphNodeMap.computeIfPresent(
					node.getParent(),
					(k, v) -> { v.getChildren().add(graphNode); return v; }
			);
		}

		// Fetch root parents if not a location
		if(!"L".equals(rootNode.getType()) && equipmentChildrenRepository != null) {
			List<EquipmentChildren> parents = equipmentChildrenRepository.getEquipmentParents(equipment);
			rootNode.setParents(parents);
		}

		// Return as List so not to break the API
		return Arrays.asList(rootNode);
	}
}
