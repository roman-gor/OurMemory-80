package com.gorman.ourmemoryapp.data.content.repository

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.testutil.FakeBurialsRepository
import com.gorman.ourmemoryapp.testutil.FakeContentRemoteDataSource
import com.gorman.ourmemoryapp.testutil.FakeToursRepository
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentEditorRepositoryImplTest {

    private val remote = FakeContentRemoteDataSource()
    private val veterans = FakeVeteransRepository()
    private val burials = FakeBurialsRepository()
    private val tours = FakeToursRepository()
    private val repository = ContentEditorRepositoryImpl(remote, veterans, burials, tours)

    @Test
    fun eachWriteRefreshesOnlyItsOwnCache() = runTest {
        repository.saveVeteran(Veteran(id = "1"))
        repository.deleteVeteran("1")
        repository.saveBurial(Burial(id = "b_001"))
        repository.saveTour(Tour(id = "t_1"))
        repository.deleteTour("t_1")

        assertEquals(5, remote.writes.size)
        assertEquals(2, veterans.invalidations)
        assertEquals(1, burials.invalidations)
        assertEquals(2, tours.invalidations)
    }
}
