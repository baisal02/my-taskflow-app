package myproject.taskflow.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import myproject.taskflow.dto.request.TaskFilter;
import myproject.taskflow.entities.Task;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class TaskSpecification implements Specification<Task> {
    private final TaskFilter filter;
    public TaskSpecification(TaskFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getPriority() != null) {
            predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
        }

        if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("category")), "%" + filter.getCategory().toLowerCase() + "%"));
        }

        if (filter.getCreatedById() != null) {
            predicates.add(cb.equal(root.get("createdBy").get("id"), filter.getCreatedById()));
        }

        if (filter.getAssignedToId() != null) {
            predicates.add(cb.equal(root.get("assignedTo").get("id"), filter.getAssignedToId()));
        }

        if (filter.getTeamId() != null) {
            predicates.add(cb.equal(root.get("team").get("id"), filter.getTeamId()));
        }

        if (filter.getDeadlineFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("deadline"), filter.getDeadlineFrom()));
        }

        if (filter.getDeadlineTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("deadline"), filter.getDeadlineTo()));
        }

        if (filter.getCreatedAtFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom()));
        }

        if (filter.getCreatedAtTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
