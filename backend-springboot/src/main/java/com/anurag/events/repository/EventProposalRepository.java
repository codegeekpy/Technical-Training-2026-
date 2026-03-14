package com.anurag.events.repository;

import com.anurag.events.entity.EventProposal;
import com.anurag.events.entity.EventProposal.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventProposalRepository extends JpaRepository<EventProposal, Long> {
    List<EventProposal> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
    List<EventProposal> findByStatusOrderByCreatedAtAsc(ProposalStatus status);
    List<EventProposal> findByStatusInOrderByCreatedAtAsc(List<ProposalStatus> statuses);

    @Query("SELECT e FROM EventProposal e ORDER BY e.createdAt DESC")
    List<EventProposal> findAllOrderByCreatedAtDesc();

    @Query("SELECT e FROM EventProposal e WHERE e.status = :status ORDER BY e.createdAt DESC")
    List<EventProposal> findByStatusOrderByCreatedAtDesc(@Param("status") ProposalStatus status);
}
