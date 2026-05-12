package com.company.lms.controller;

import com.company.lms.service.StatisticsService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class DashboardController implements Serializable {

    @Inject
    private StatisticsService statsService;

    private PieChartModel pieModel;
    private BarChartModel barModel;
    
    private LocalDateTime lastUpdated;
    private long totalRequests;
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;

    @PostConstruct
    public void init() {
        this.lastUpdated = LocalDateTime.now();
        createPieModelAndKPIs();
        createBarModel();
    }

    private void createPieModelAndKPIs() {
        pieModel = new PieChartModel();
        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();

        Map<String, Long> stats = statsService.getLeavesByStatus();
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();

        long total = 0;
        long approved = 0;
        long pending = 0;
        long rejected = 0;

        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            String status = entry.getKey();
            Long count = entry.getValue();
            
            labels.add(status);
            values.add(count);
            total += count;
            
            if ("Εγκρίθηκε".equals(status)) {
                bgColors.add("#28a745");
                approved = count;
            } else if ("Εκκρεμεί".equals(status)) {
                bgColors.add("#ed5929");
                pending = count;
            } else {
                bgColors.add("#dc3545");
                rejected += count;
            }
        }

        this.totalRequests = total;
        this.approvedCount = approved;
        this.pendingCount = pending;
        this.rejectedCount = rejected;

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        pieModel.setData(data);
        
        PieChartOptions options = new PieChartOptions();
        options.setMaintainAspectRatio(false);
        pieModel.setOptions(options);
    }

    private void createBarModel() {
        barModel = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("Άδειες ανά Τύπο");

        Map<String, Long> stats = statsService.getLeavesByType();
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            labels.add(entry.getKey());
            values.add(entry.getValue());
        }

        barDataSet.setData(values);
        barDataSet.setBackgroundColor("#0188ca"); 
        data.addChartDataSet(barDataSet);
        data.setLabels(labels);
        barModel.setData(data);
        
        BarChartOptions options = new BarChartOptions();
        options.setMaintainAspectRatio(false);
        barModel.setOptions(options);
    }

    public PieChartModel getPieModel() { return pieModel; }
    public BarChartModel getBarModel() { return barModel; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public long getTotalRequests() { return totalRequests; }
    public long getPendingCount() { return pendingCount; }
    public long getApprovedCount() { return approvedCount; }
    public long getRejectedCount() { return rejectedCount; }
}