package com.hackathon3.grummang_hack.service.file;

import com.hackathon3.grummang_hack.model.dto.file.FileHistoryBySaaS;
import com.hackathon3.grummang_hack.model.dto.file.FileRelationEdges;
import com.hackathon3.grummang_hack.model.dto.file.FileRelationNodes;
import com.hackathon3.grummang_hack.model.entity.Activities;
import com.hackathon3.grummang_hack.repository.ActivitiesRepo;
import com.hackathon3.grummang_hack.repository.FileGroupRepo;
import com.hackathon3.grummang_hack.repository.FileUploadTableRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileVisualizeTestService {
    private final ActivitiesRepo activitiesRepo;
    private final FileUploadTableRepo fileUploadRepo;
    private final FileGroupRepo fileGroupRepo;
    private final FileSimilarService fileSimilarService;
    private static final String FILE_UPLOAD = "file_upload";
    private static final String SLACK = "slack";
    private static final String GOOGLE_DRIVE = "googleDrive";

    public FileVisualizeTestService(ActivitiesRepo activitiesRepo, FileUploadTableRepo fileUploadRepo, FileGroupRepo fileGroupRepo, FileSimilarService fileSimilarService) {
        this.activitiesRepo = activitiesRepo;
        this.fileUploadRepo = fileUploadRepo;
        this.fileGroupRepo = fileGroupRepo;
        this.fileSimilarService = fileSimilarService;
    }

    // a->b, b->c가 만족할 때, 굳이 a->c로 연결하지 않음.
    private List<FileRelationEdges> filterTransitiveEdges(List<FileRelationEdges> edges) {
        // Maps to track adjacency and labels
        Map<Long, Set<Long>> adjacencyMap = new HashMap<>();
        Map<String, Set<String>> edgeLabelsMap = new HashMap<>();

        // Build adjacency and label maps
        for (FileRelationEdges edge : edges) {
            adjacencyMap
                    .computeIfAbsent(edge.getSource(), k -> new HashSet<>())
                    .add(edge.getTarget());
            edgeLabelsMap.computeIfAbsent(edge.getSource() + "-" + edge.getTarget(), k -> new HashSet<>()).add(edge.getLabel());
        }

        // Set to track filtered edges
        Set<String> filteredEdges = new HashSet<>();

        // Add only non-transitive edges
        for (FileRelationEdges edge : edges) {
            if (!isTransitive(edge.getSource(), edge.getTarget(), adjacencyMap)) {
                filteredEdges.add(edge.getSource() + "-" + edge.getTarget());
            }
        }

        // Construct final list of edges
        return filteredEdges.stream()
                .map(edgeKey -> {
                    String[] parts = edgeKey.split("-");
                    // Retrieve the appropriate label from edgeLabelsMap (assuming one label per edge)
                    String label = edgeLabelsMap.get(edgeKey).iterator().next();
                    return new FileRelationEdges(Long.parseLong(parts[0]), Long.parseLong(parts[1]), label);
                })
                .toList();
    }

    // 특정 엣지가 전이적인지 확인
    // 출발지(source)에서 도착지(target)까지의 간접 경로가 존재하는지 확인하여, 전이적인 경우 true를 반환하고, 그렇지 않으면 false를 반환
    private boolean isTransitive(Long source, Long target, Map<Long, Set<Long>> adjacencyMap) {
        if (!adjacencyMap.containsKey(source)) return false;

        Set<Long> directTargets = adjacencyMap.get(source);
        for (Long intermediate : directTargets) {
            if (adjacencyMap.containsKey(intermediate) && adjacencyMap.get(intermediate).contains(target)) {
                return true; // Transitive relationship found
            }
        }
        return false;
    }

    //활동(Activity) 데이터를 가져오고 시작 활동을 기준으로 히스토리를 추적합니다.
    //파일 히스토리 맵을 초기화하고, 노드와 엣지를 생성합니다.
    //DFS를 통해 파일 간의 관계를 탐색하고, 노드 및 엣지 정보를 갱신합니다.
    //필요한 정보를 필터링한 후 최종적으로 Slack과 Google Drive에 해당하는 파일 히스토리 및 엣지 데이터를 반환합니다.
    public FileHistoryBySaaS getFileHistoryBySaaS(long eventId, long orgId) {
        //활동(Activity) 데이터를 가져오고 시작 활동을 기준으로 히스토리를 추적합니다.
        Activities activity = getActivity(eventId);
        Activities startActivity = activitiesRepo.getActivitiesBySaaSFileId(activity.getSaasFileId());

        //파일 히스토리 맵을 초기화하고, 노드와 엣지를 생성합니다.
        Map<String, List<FileRelationNodes>> fileHistoryMap = initializeFileHistoryMap();
        Map<Long, FileRelationNodes> nodesMap = new HashMap<>();
        List<FileRelationEdges> edges = new ArrayList<>();
        Set<Long> seenEventIds = new HashSet<>();

        //DFS를 통해 파일 간의 관계를 탐색하고, 노드 및 엣지 정보를 갱신합니다.
        exploreFileRelationsDFS(startActivity, 2, seenEventIds, nodesMap, edges, eventId);
        addGroupRelatedActivities(eventId, seenEventIds, nodesMap, orgId);

        String saasName = getSaasName(startActivity);
        List<FileRelationNodes> nodesList = new ArrayList<>(nodesMap.values());
        populateFileHistoryMap(fileHistoryMap, saasName, nodesList);
        //필요한 정보를 필터링한 후 최종적으로 Slack과 Google Drive에 해당하는 파일 히스토리 및 엣지 데이터를 반환합니다.
        // Filter out transitive edges
        List<FileRelationEdges> filteredEdges = filterTransitiveEdges(edges);

        return FileHistoryBySaaS.builder()
                .originNode(eventId)
                .slack(fileHistoryMap.get(SLACK))
                .googleDrive(fileHistoryMap.get(GOOGLE_DRIVE))
                .edges(filteredEdges)
                .build();
    }

    private void addGroupRelatedActivities(long eventId, Set<Long> seenEventIds, Map<Long, FileRelationNodes> nodesMap, long orgId) {
        String groupName = fileGroupRepo.findGroupNameById(eventId);
        List<Activities> sameGroups = activitiesRepo.findByOrgIdAndGroupName(orgId, groupName);
        for (Activities a : sameGroups) {
            if (!seenEventIds.contains(a.getId()) && !a.getId().equals(eventId)) {
                FileRelationNodes targetNode = createFileRelationNodes(a, eventId);
                nodesMap.putIfAbsent(a.getId(), targetNode);
            }
        }
    }

    private Activities getActivity(long eventId) {
        return activitiesRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
    }

    //파일 히스토리를 저장할 맵을 초기화
    private Map<String, List<FileRelationNodes>> initializeFileHistoryMap() {
        Map<String, List<FileRelationNodes>> fileHistoryMap = new HashMap<>();
        fileHistoryMap.put(SLACK, new ArrayList<>());
        fileHistoryMap.put(GOOGLE_DRIVE, new ArrayList<>());
        return fileHistoryMap;
    }

    private String getSaasName(Activities activity) {
        return activity.getUser().getOrgSaaS().getSaas().getSaasName().toLowerCase();
    }

    // SaaS 이름에 따라 해당 리스트에 노드 정보를 추가
    private void populateFileHistoryMap(Map<String, List<FileRelationNodes>> fileHistoryMap, String saasName, List<FileRelationNodes> nodesList) {
        // nodesList를 eventTs 필드를 기준으로 오름차순 정렬
        nodesList.sort(Comparator.comparing(FileRelationNodes::getEventTs));

        // saasName에 따라 파일 히스토리 맵에 정렬된 노드 리스트를 추가
        if (SLACK.equals(saasName)) {
            fileHistoryMap.get(SLACK).addAll(nodesList);
        } else if (GOOGLE_DRIVE.equals(saasName)) {
            fileHistoryMap.get(GOOGLE_DRIVE).addAll(nodesList);
        }
    }

    //현재 활동을 처리하고, 이미 탐색된 활동을 추적하여 중복을 방지합니다.
    //SaaSFileID와 해시 값이 일치하는 활동들을 탐색하여 엣지를 추가합니다.
    //재귀적으로 깊이를 줄여가며 탐색을 진행합니다.
    private void exploreFileRelationsDFS(Activities startActivity, int maxDepth, Set<Long> seenEventIds, Map<Long, FileRelationNodes> nodesMap, List<FileRelationEdges> edges, long eventId) {
        if (maxDepth < 0) return;

        // 현재 활동 처리
        log.info("시작 노드: {}", startActivity.getId());

        // 초기 활동 결정
        Activities initialActivity = determineInitialActivity(startActivity, seenEventIds);
        log.info("업로드 이벤트 노드(시작 노드 재설정): {}", initialActivity.getId());


        // SaaSFileID와 Hash로 일치하는 활동 목록 가져오기
        List<Activities> sameSaasFiles = findAndSortActivitiesBySaasFileId(initialActivity);
        List<Activities> sameHashFiles = findAndSortActivitiesByHash(initialActivity);
        log.info("SaaS파일id가 같음: " + sameSaasFiles.stream().map(Activities::getId).collect(Collectors.toList()));

        // sameHashFiles의 id 리스트 출력
        log.info("해시256값이 같음: " + sameHashFiles.stream().map(Activities::getId).collect(Collectors.toList()));

        // SaaSFileID로 일치하는 활동을 Hash 목록에서 제거
        removeDuplicateActivities(sameHashFiles, sameSaasFiles);

        // 새로운 초기 활동 설정
        Activities newInitialActivity = determineNewInitialActivity(startActivity, initialActivity, sameSaasFiles, sameHashFiles, seenEventIds);

        if (newInitialActivity != initialActivity) {
            // 새로운 초기 활동이 설정되면 해당 활동에 대한 정보 갱신
            sameSaasFiles = findAndSortActivitiesBySaasFileId(newInitialActivity);
            sameHashFiles = findAndSortActivitiesByHash(newInitialActivity);
            removeDuplicateActivities(sameHashFiles, sameSaasFiles);
        }

        // 연관된 활동들에 대한 처리
        processRelatedActivities(newInitialActivity, sameSaasFiles, sameHashFiles, seenEventIds, nodesMap, edges, maxDepth, eventId);
    }

    private Activities determineInitialActivity(Activities startActivity, Set<Long> seenEventIds) {
        Activities testActivity = activitiesRepo.getActivitiesBySaaSFileId(startActivity.getSaasFileId());
        if (!startActivity.getEventType().equals(FILE_UPLOAD) && !seenEventIds.contains(testActivity.getId())) {
            return testActivity;
        } else {
            return startActivity;
        }
    }

    private List<Activities> findAndSortActivitiesBySaasFileId(Activities activity) {
        return activitiesRepo.findListBySaasFileId(activity.getSaasFileId())
                .stream()
                .filter(a -> !a.getId().equals(activity.getId()))  // 초기 활동의 ID가 아닌 활동만 필터링
                .sorted(Comparator.comparing(Activities::getEventTs))  // 시간 순서로 정렬
                .collect(Collectors.toList());
    }

    private List<Activities> findAndSortActivitiesByHash(Activities activity) {
        return activitiesRepo.findByHash(getSaltedHash(activity))
                .stream()
                .filter(a -> FILE_UPLOAD.equals(a.getEventType()))  // 'file_upload' 타입만 필터링 (지금 이것때문에 fileChanged에서 나온 노드 하나가 추적이 안됨)
                .sorted(Comparator.comparing(Activities::getEventTs))  // 시간 순서로 정렬
                .collect(Collectors.toList());
    }

    private void removeDuplicateActivities(List<Activities> sameHashFiles, List<Activities> sameSaasFiles) {
        sameHashFiles.removeAll(sameSaasFiles);
    }

    private Activities determineNewInitialActivity(Activities startActivity, Activities initialActivity, List<Activities> sameSaasFiles, List<Activities> sameHashFiles, Set<Long> seenEventIds) {
        if (sameSaasFiles.isEmpty() && !sameHashFiles.isEmpty() && !sameHashFiles.getFirst().getId().equals(startActivity.getId()) && !seenEventIds.contains(sameHashFiles.getFirst().getId())) {
            Activities newInitialActivity = sameHashFiles.getFirst();
            log.info("새로운 기준 노드 부여: {}", newInitialActivity.getId());
            return newInitialActivity;
        }
        return initialActivity;
    }

    private void processRelatedActivities(Activities initialActivity, List<Activities> sameSaasFiles, List<Activities> sameHashFiles, Set<Long> seenEventIds, Map<Long, FileRelationNodes> nodesMap, List<FileRelationEdges> edges, int maxDepth, long eventId) {
        // SaaSFileID로 일치하는 활동들에 대해 연결 추가
        addRelatedActivities(sameSaasFiles, initialActivity, seenEventIds, nodesMap, edges, "File_SaaS_Match", maxDepth, eventId);

        // Hash로 일치하는 활동들에 대해 연결 추가
        addRelatedActivities(sameHashFiles, initialActivity, seenEventIds, nodesMap, edges, "File_Hash_Match", maxDepth, eventId);

        log.info("----------SaaSFileId랑 Hash값 둘다 봤음------------");
    }

    //주어진 활동들에 대해 노드를 생성하고, 엣지를 추가합니다.
    //재귀적으로 DFS 탐색을 진행하여 연결 관계를 계속해서 추적합니다.
    private void addRelatedActivities(List<Activities> relatedActivities, Activities startActivity, Set<Long> seenEventIds, Map<Long, FileRelationNodes> nodesMap, List<FileRelationEdges> edges, String edgeType, int currentDepth, long eventId) {
        // 활동 리스트를 이벤트 발생 시간 기준으로 정렬 (오름차순)
        log.info("기준 노드: {}", startActivity.getId());
        processCurrentActivity(startActivity, seenEventIds, nodesMap, eventId);
        relatedActivities.sort(Comparator.comparing(Activities::getEventTs));
        log.info("seenEventIds에 들어갔냐? :{}", seenEventIds.contains(startActivity.getId()));
        for (Activities relatedActivity : relatedActivities) {
            log.info("기준 노드에서 탐색할 노드: {}", relatedActivity.getId());
            if (!seenEventIds.contains(relatedActivity.getId()) && !relatedActivity.getId().equals(startActivity.getId())) {
                FileRelationNodes targetNode = createFileRelationNodes(relatedActivity, eventId);
                nodesMap.putIfAbsent(relatedActivity.getId(), targetNode);

                log.info("기준 , 탐색 : {}, {}", startActivity.getId(), relatedActivity.getId());
                // 엣지 추가 (startActivity와 시간 순으로 연결된 relatedActivity)
                edges.add(new FileRelationEdges(startActivity.getId(), relatedActivity.getId(), edgeType));

                // DFS 탐색 계속 진행 (depth 감소)
                if (currentDepth > 0) {
                    exploreFileRelationsDFS(relatedActivity, currentDepth - 1, seenEventIds, nodesMap, edges, eventId);
                }

                // 다음 연결을 위해 startActivity를 현재 relatedActivity로 업데이트
                if (relatedActivity.getEventType().equals(FILE_UPLOAD)) {
                    startActivity = relatedActivity;
                } else {
                    startActivity = activitiesRepo.getActivitiesBySaaSFileId(relatedActivity.getSaasFileId());
                }
                log.info("다음 노드:{}", startActivity.getId());
            }
        }
    }

    //활동을 처리하고 노드를 생성한 후, 해당 활동이 이미 처리되었음을 기록
    private void processCurrentActivity(Activities activity, Set<Long> seenEventIds, Map<Long, FileRelationNodes> nodesMap, long eventId) {
        seenEventIds.add(activity.getId());
        FileRelationNodes node = createFileRelationNodes(activity, eventId);
        nodesMap.putIfAbsent(activity.getId(), node);
    }

    //Activities 객체를 기반으로 파일 관계 노드(FileRelationNodes) 객체를 생성
    private FileRelationNodes createFileRelationNodes(Activities activity, long eventId) {
        double similarity = fileSimilarService.getFileSimilarity(activity.getId(), eventId);
        BigDecimal roundedSimilarity = BigDecimal.valueOf(similarity).setScale(2, RoundingMode.HALF_UP);
        return FileRelationNodes.builder()
                .eventId(activity.getId())
                .saas(activity.getUser().getOrgSaaS().getSaas().getSaasName())
                .eventType(activity.getEventType())
                .fileName(activity.getFileName())
                .hash256(getSaltedHash(activity))
                .saasFileId(activity.getSaasFileId())
                .eventTs(activity.getEventTs())
                .email(activity.getUser().getEmail())
                .uploadChannel(activity.getUploadChannel())
                .similarity(roundedSimilarity.doubleValue())
                .build();
    }

    private String getSaltedHash(Activities activity) {
        return fileUploadRepo.findHashByOrgSaaS_IdAndSaasFileId(activity.getUser().getOrgSaaS().getId(), activity.getSaasFileId(), activity.getEventTs());
    }
}