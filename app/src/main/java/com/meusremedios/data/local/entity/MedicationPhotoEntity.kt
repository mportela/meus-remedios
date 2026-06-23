package com.meusremedios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.meusremedios.domain.model.PhotoSide

/**
 * Entidade Room de uma foto de medicamento. Os vetores [embedding] e
 * [dominantColorLab] são persistidos como BLOB pelos type converters.
 */
@Entity(
    tableName = "medication_photos",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["medication_id"])],
)
data class MedicationPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "medication_id")
    val medicationId: Long,
    @ColumnInfo(name = "file_path")
    val filePath: String,
    val side: PhotoSide,
    val embedding: FloatArray?,
    @ColumnInfo(name = "dominant_color_lab")
    val dominantColorLab: FloatArray?,
    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: Float?,
    @ColumnInfo(name = "imprint_text")
    val imprintText: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MedicationPhotoEntity) return false
        return id == other.id &&
            medicationId == other.medicationId &&
            filePath == other.filePath &&
            side == other.side &&
            embedding.contentEqualsNullable(other.embedding) &&
            dominantColorLab.contentEqualsNullable(other.dominantColorLab) &&
            aspectRatio == other.aspectRatio &&
            imprintText == other.imprintText &&
            createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + medicationId.hashCode()
        result = 31 * result + filePath.hashCode()
        result = 31 * result + side.hashCode()
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        result = 31 * result + (dominantColorLab?.contentHashCode() ?: 0)
        result = 31 * result + (aspectRatio?.hashCode() ?: 0)
        result = 31 * result + (imprintText?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

private fun FloatArray?.contentEqualsNullable(other: FloatArray?): Boolean =
    when {
        this == null && other == null -> true
        this == null || other == null -> false
        else -> this.contentEquals(other)
    }
