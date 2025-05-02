package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Station;
import com.fourt.railskylines.domain.Train;
import com.fourt.railskylines.domain.dto.ResultPaginationDTO;
import com.fourt.railskylines.repository.StationRepository;
import com.fourt.railskylines.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Station handleCreateStation(Station station) {
        // Validate stationName
        if (station.getStationName() == null || station.getStationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Station name must not be empty");
        }

        // Validate position
        if (station.getPosition() < 0) {
            throw new IllegalArgumentException("Position must be non-negative");
        }

        // Save station
        return stationRepository.save(station);
    }

    public Station fetchStationById(long id) {
        Optional<Station> stationOptional = stationRepository.findById(id);
        if (stationOptional.isPresent()) {
            return stationOptional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllStations(Specification<Station> spec, Pageable pageable) {
        Page<Station> pageStation = stationRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageStation.getTotalPages());
        mt.setTotal(pageStation.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageStation.getContent());
        return rs;
    }

    public Station handleUpdateStation(long id, Station station) throws IdInvalidException {
        Station existingStation = fetchStationById(id);
        if (existingStation == null) {
            throw new IdInvalidException("Station with id = " + id + " does not exist");
        }

        if (station.getStationName() == null || station.getStationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Station name must not be empty");
        }

        if (station.getPosition() < 0) {
            throw new IllegalArgumentException("Position must be non-negative");
        }

        // Update fields
        existingStation.setStationName(station.getStationName());
        existingStation.setPosition(station.getPosition());
        existingStation.setRoutes(station.getRoutes());

        return stationRepository.save(existingStation);
    }

    public void handleDeleteStation(long id) throws IdInvalidException {
        if (!stationRepository.existsById(id)) {
            throw new IdInvalidException("Station with id = " + id + " does not exist");
        }
        stationRepository.deleteById(id);
    }

    public boolean isStationExist(String stationName) {
        return this.stationRepository.existsByStationName(stationName);
    }

}