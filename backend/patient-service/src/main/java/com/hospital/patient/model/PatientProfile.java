package com.hospital.patient.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hospital.common.domain.BaseEntity;
import com.hospital.patient.domain.Patient;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* 360-Derece Hasta Profili - Comprehensive Patient Profile
* Hastanın tüm tıbbi, demografik, sosyal ve davranışsal verilerini içeren kapsamlı profil
*
* Özellikler:
* - Demografik bilgiler ve iletişim detayları
* - Tıbbi geçmiş ve kronik hastalıklar
* - Aile tıbbi geçmişi
* - Yaşam tarzı ve risk faktörleri
* - Sosyal determinantlar
* - Davranışsal sağlık verileri
* - Wearable device verileri
* - AI risk skorları
* - Tedavi tercihleri ve hedefleri
*/
@Entity
@Table(name = "patient_profiles")
@Data
@EqualsAndHashCode(callSuper = false)
public class PatientProfile extends BaseEntity {
    private static final int MAX_SCORE = 100;
    private static final int SCORE_80 = 80;
    private static final int SCORE_60 = 60;
    private static final int SCORE_50 = 50;
    private static final int SCORE_40 = 40;
    private static final int SCORE_30 = 30;
    private static final int SCORE_27 = 27;
    private static final int SCORE_21 = 21;
    private static final int SCORE_20 = 20;
    private static final int SCORE_10 = 10;
    private static final int SCORE_6 = 6;
  /** Id. */

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /** Patient. */

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  @JsonIgnore
  private Patient patient;

  // === DEMOGRAFIK BİLGİLER ===
  @Column(name = "preferred_language", length = SCORE_10)
  private String preferredLanguage = "tr"; // tr, en, de
  /** Widowed. */

  @Column(name = "marital_status", length = SCORE_20)
  private String maritalStatus; // single, married, divorced, widowed
  /** Postgraduate. */

  @Column(name = "education_level", length = SCORE_30)
  private String educationLevel; // primary, secondary, university, postgraduate
  /** Occupation. */

  @Column(name = "occupation", length = MAX_SCORE)
  private String occupation;
  /** High. */

  @Column(name = "income_level", length = SCORE_20)
  private String incomeLevel; // low, medium, high
  /** None. */

  @Column(name = "insurance_type", length = SCORE_50)
  private String insuranceType; // public, private, none

  // === TIBBİ GEÇMİŞ ===
  @ElementCollection
  @CollectionTable(name = "patient_chronic_conditions",
  joinColumns = @JoinColumn(name = "profile_id"))
  @Column(name = "condition")
  private Set<String> chronicConditions = new HashSet<>(); // diabetes, hypertension, asthma, etc.

  @ElementCollection
  @CollectionTable(name = "patient_allergies",
  joinColumns = @JoinColumn(name = "profile_id"))
  @Column(name = "allergy")
  private Set<String> allergies = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "patient_medications",
  joinColumns = @JoinColumn(name = "profile_id"))
  @Column(name = "medication")
  private Set<String> currentMedications = new HashSet<>();
  /** Previous surgeries. */

  @Column(name = "previous_surgeries", columnDefinition = "TEXT")
  private String previousSurgeries;
  /** Previous hospitalizations. */

  @Column(name = "previous_hospitalizations", columnDefinition = "TEXT")
  private String previousHospitalizations;

  // === AİLE TIBBİ GEÇMİŞİ ===
  @ElementCollection
  @CollectionTable(name = "patient_family_history",
  joinColumns = @JoinColumn(name = "profile_id"))
  @MapKeyColumn(name = "condition")
  @Column(name = "family_member")
  private Map<String, String> familyMedicalHistory = new HashMap<>(); // condition -> family_member

  // === YAŞAM TARZI VE RİSK FAKTÖRLERİ ===
  /** Current. */
  @Column(name = "smoking_status", length = SCORE_20)
  private String smokingStatus; // never, former, current
  /** Smoking pack years. */

  @Column(name = "smoking_pack_years")
  private Integer smokingPackYears;
  /** Heavy. */

  @Column(name = "alcohol_consumption", length = SCORE_20)
  private String alcoholConsumption; // none, light, moderate, heavy
  /** Intensive. */

  @Column(name = "exercise_frequency", length = SCORE_20)
  private String exerciseFrequency; // none, rare, regular, intensive
  /** Etc.. */

  @Column(name = "diet_type", length = SCORE_30)
  private String dietType; // standard, vegetarian, vegan, mediterranean, etc.
  /** Sleep hours per night. */

  @Column(name = "sleep_hours_per_night")
  private Double sleepHoursPerNight;
  /** Scale. */

  @Column(name = "stress_level")
  @Min(1) @Max(SCORE_10)
  private Integer stressLevel; // 1-SCORE_10 scale

  // === SOSYAL DETERMINANTLAR ===
  /** Etc.. */
  @Column(name = "living_situation", length = SCORE_50)
  private String livingSituation; // alone, family, assisted_living, etc.
  /** Scale. */

  @Column(name = "social_support_level")
  @Min(1) @Max(SCORE_10)
  private Integer socialSupportLevel; // 1-SCORE_10 scale
  /** Transportation access. */

  @Column(name = "transportation_access")
  private Boolean transportationAccess;
  /** Food security. */

  @Column(name = "food_security")
  private Boolean foodSecurity;
  /** Housing stability. */

  @Column(name = "housing_stability")
  private Boolean housingStability;

  // === DAVRANIŞSAL SAĞLIK ===
  /** Scale. */
  @Column(name = "health_literacy_level")
  @Min(1) @Max(SCORE_10)
  private Integer healthLiteracyLevel; // 1-SCORE_10 scale
  /** 0-max_score%. */

  @Column(name = "medication_adherence_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer medicationAdherenceScore; // 0-MAX_SCORE%
  /** 0-max_score%. */

  @Column(name = "appointment_adherence_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer appointmentAdherenceScore; // 0-MAX_SCORE%
  /** Scale. */

  @Column(name = "preventive_care_engagement")
  @Min(1) @Max(SCORE_10)
  private Integer preventiveCareEngagement; // 1-SCORE_10 scale

  // === WEARABLE DEVICE VERİLERİ ===
  /** Etc.. */
  @Column(name = "wearable_device_type", length = SCORE_50)
  private String wearableDeviceType; // apple_watch, fitbit, garmin, etc.
  /** Average daily steps. */

  @Column(name = "average_daily_steps")
  private Integer averageDailySteps;
  /** Average heart rate. */

  @Column(name = "average_heart_rate")
  private Integer averageHeartRate;
  /** Average sleep score. */

  @Column(name = "average_sleep_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer averageSleepScore;
  /** Last wearable sync. */

  @Column(name = "last_wearable_sync")
  private LocalDateTime lastWearableSync;

  // === AI RİSK SKORLARI ===
  /** 0-max_score%. */
  @Column(name = "cardiovascular_risk_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer cardiovascularRiskScore; // 0-MAX_SCORE%
  /** 0-max_score%. */

  @Column(name = "diabetes_risk_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer diabetesRiskScore; // 0-MAX_SCORE%
  /** 0-max_score%. */

  @Column(name = "readmission_risk_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer readmissionRiskScore; // 0-MAX_SCORE%
  /** 0-max_score%. */

  @Column(name = "mortality_risk_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer mortalityRiskScore; // 0-MAX_SCORE%
  /** 0-max_score%. */

  @Column(name = "overall_health_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer overallHealthScore; // 0-MAX_SCORE%
  /** Risk scores last updated. */

  @Column(name = "risk_scores_last_updated")
  private LocalDateTime riskScoresLastUpdated;

  // === TEDAVİ TERCİHLERİ ===
  /** App. */
  @Column(name = "preferred_communication_method", length = SCORE_20)
  private String preferredCommunicationMethod; // phone, email, sms, app
  /** Evening. */

  @Column(name = "preferred_appointment_time", length = SCORE_20)
  private String preferredAppointmentTime; // morning, afternoon, evening
  /** Treatment goals. */

  @Column(name = "treatment_goals", columnDefinition = "TEXT")
  private String treatmentGoals;
  /** Care preferences. */

  @Column(name = "care_preferences", columnDefinition = "TEXT")
  private String carePreferences;
  /** Advance directives. */

  @Column(name = "advance_directives")
  private Boolean advanceDirectives;

  // === MENTAl SAĞLIK ===
  /**
   * Score.
   * @param 0-SCORE_27 0-score_27.
   * @return Result.
   */
  @Column(name = "depression_screening_score")
  @Min(0) @Max(SCORE_27)
  private Integer depressionScreeningScore; // PHQ-9 score (0-SCORE_27)
  /**
   * Score.
   * @param 0-SCORE_21 0-score_21.
   * @return Result.
   */

  @Column(name = "anxiety_screening_score")
  @Min(0) @Max(SCORE_21)
  private Integer anxietyScreeningScore; // GAD-7 score (0-SCORE_21)
  /** Mental health history. */

  @Column(name = "mental_health_history", columnDefinition = "TEXT")
  private String mentalHealthHistory;

  // === SOSYAL VE KÜLTÜREL FAKTÖRLER ===
  /** Cultural background. */
  @Column(name = "cultural_background", length = SCORE_50)
  private String culturalBackground;
  /** Religious preferences. */

  @Column(name = "religious_preferences", length = SCORE_50)
  private String religiousPreferences;
  /** Dietary restrictions. */

  @Column(name = "dietary_restrictions", columnDefinition = "TEXT")
  private String dietaryRestrictions;

  // === EMERGENCY CONTACTS ===
  @ElementCollection
  @CollectionTable(name = "patient_emergency_contacts",
  joinColumns = @JoinColumn(name = "profile_id"))
  private List<EmergencyContact> emergencyContacts = new ArrayList<>();

  // === AUDIT VE METADATA ===
  /** Created at. */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  /** Updated at. */

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
  /** Last comprehensive assessment. */

  @Column(name = "last_comprehensive_assessment")
  private LocalDateTime lastComprehensiveAssessment;
  /** 0-max_score%. */

  @Column(name = "profile_completeness_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer profileCompletenessScore; // 0-MAX_SCORE%
  /** 0-max_score%. */

  @Column(name = "data_quality_score")
  @Min(0) @Max(MAX_SCORE)
  private Integer dataQualityScore; // 0-MAX_SCORE%

  // === HELPER METHODS ===

  /**
  * Profil tamamlanma yüzdesini hesaplar
  */
  public void calculateCompletenessScore() {
    int totalFields = SCORE_50; // Total number of important fields
    int completedFields = 0;

    // Count completed fields
    if (preferredLanguage != null) {
      completedFields++;
    }
    if (maritalStatus != null) {
      completedFields++;
    }
    if (educationLevel != null) {
      completedFields++;
    }
    if (occupation != null) {
      completedFields++;
    }
    if (incomeLevel != null) {
      completedFields++;
    }
    if (insuranceType != null) {
      completedFields++;
    }
    if (!chronicConditions.isEmpty()) {
      completedFields++;
    }
    if (!allergies.isEmpty()) {
      completedFields++;
    }
    if (!currentMedications.isEmpty()) {
      completedFields++;
    }
    if (previousSurgeries != null) {
      completedFields++;
    }
    if (previousHospitalizations != null) {
      completedFields++;
    }
    if (!familyMedicalHistory.isEmpty()) {
      completedFields++;
    }
    if (smokingStatus != null) {
      completedFields++;
    }
    if (alcoholConsumption != null) {
      completedFields++;
    }
    if (exerciseFrequency != null) {
      completedFields++;
    }
    if (dietType != null) {
      completedFields++;
    }
    if (sleepHoursPerNight != null) {
      completedFields++;
    }
    if (stressLevel != null) {
      completedFields++;
    }
    if (livingSituation != null) {
      completedFields++;
    }
    if (socialSupportLevel != null) {
      completedFields++;
    }
    if (transportationAccess != null) {
      completedFields++;
    }
    if (foodSecurity != null) {
      completedFields++;
    }
    if (housingStability != null) {
      completedFields++;
    }
    if (healthLiteracyLevel != null) {
      completedFields++;
    }
    if (medicationAdherenceScore != null) {
      completedFields++;
    }
    if (appointmentAdherenceScore != null) {
      completedFields++;
    }
    if (preventiveCareEngagement != null) {
      completedFields++;
    }
    if (wearableDeviceType != null) {
      completedFields++;
    }
    if (averageDailySteps != null) {
      completedFields++;
    }
    if (averageHeartRate != null) {
      completedFields++;
    }
    if (averageSleepScore != null) {
      completedFields++;
    }
    if (cardiovascularRiskScore != null) {
      completedFields++;
    }
    if (diabetesRiskScore != null) {
      completedFields++;
    }
    if (readmissionRiskScore != null) {
      completedFields++;
    }
    if (mortalityRiskScore != null) {
      completedFields++;
    }
    if (overallHealthScore != null) {
      completedFields++;
    }
    if (preferredCommunicationMethod != null) {
      completedFields++;
    }
    if (preferredAppointmentTime != null) {
      completedFields++;
    }
    if (treatmentGoals != null) {
      completedFields++;
    }
    if (carePreferences != null) {
      completedFields++;
    }
    if (advanceDirectives != null) {
      completedFields++;
    }
    if (depressionScreeningScore != null) {
      completedFields++;
    }
    if (anxietyScreeningScore != null) {
      completedFields++;
    }
    if (mentalHealthHistory != null) {
      completedFields++;
    }
    if (culturalBackground != null) {
      completedFields++;
    }
    if (religiousPreferences != null) {
      completedFields++;
    }
    if (dietaryRestrictions != null) {
      completedFields++;
    }
    if (!emergencyContacts.isEmpty()) {
      completedFields++;
    }

    this.profileCompletenessScore = (int) ((double) completedFields / totalFields * MAX_SCORE);
  }

  /**
  * Genel sağlık riskini hesaplar
  */
  public String getOverallRiskLevel() {
    if (overallHealthScore == null) {
      return "UNKNOWN";
    }

    if (overallHealthScore >= SCORE_80) {
      return "LOW";
    }
    else if (overallHealthScore >= SCORE_60) return "MODERATE";
    else if (overallHealthScore >= SCORE_40) return "HIGH";
    else return "VERY_HIGH";
  }

  /**
  * Hasta profilinin güncelliğini kontrol eder
  */
  public boolean isProfileCurrent() {
    if (lastComprehensiveAssessment == null) {
      return false;
    }
    return lastComprehensiveAssessment.isAfter(LocalDateTime.now().minusMonths(SCORE_6));
  }

  /**
  * Wearable data güncelliğini kontrol eder
  */
  public boolean isWearableDataCurrent() {
    if (lastWearableSync == null) {
      return false;
    }
    return lastWearableSync.isAfter(LocalDateTime.now().minusDays(1));
  }

  /**
  * Risk skorlarının güncelliğini kontrol eder
  */
  public boolean areRiskScoresCurrent() {
    if (riskScoresLastUpdated == null) {
      return false;
    }
    return riskScoresLastUpdated.isAfter(LocalDateTime.now().minusWeeks(1));
  }

  @Embeddable
  @Data
  public static class EmergencyContact {
    /** Name. */
    @Column(name = "name", length = MAX_SCORE)
    private String name;
    /** Relationship. */

    @Column(name = "relationship", length = SCORE_50)
    private String relationship;
    /** Phone. */

    @Column(name = "phone", length = SCORE_20)
    private String phone;
    /** Email. */

    @Column(name = "email", length = MAX_SCORE)
    private String email;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;
  }
}
